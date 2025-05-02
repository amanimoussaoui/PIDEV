from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
import onnxruntime as ort
import os
import traceback
import logging
import joblib

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(os.path.join('prediction', 'api.log')),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger('predict_api')

# Initialize Flask app
app = Flask(__name__)

# Load preprocessor for proper feature transformation
try:
    preprocessor_path = os.path.join('prediction', 'preprocessor.joblib')
    if os.path.exists(preprocessor_path):
        preprocessor = joblib.load(preprocessor_path)
        logger.info(f"Loaded preprocessor from: {preprocessor_path}")
    else:
        logger.warning(f"Preprocessor not found at: {preprocessor_path}")
        preprocessor = None
except Exception as e:
    logger.error(f"Error loading preprocessor: {e}")
    preprocessor = None

# Load feature names for reference
try:
    feature_names_path = os.path.join('prediction', 'feature_names.joblib')
    if os.path.exists(feature_names_path):
        feature_names = joblib.load(feature_names_path)
        logger.info(f"Loaded feature names from: {feature_names_path}")
        logger.info(f"Model expects {len(feature_names)} features")
    else:
        logger.warning(f"Feature names not found at: {feature_names_path}")
        feature_names = None
except Exception as e:
    logger.error(f"Error loading feature names: {e}")
    feature_names = None

# This is the original expected columns list
base_expected_columns = [
    'culture_id', 'crop_type_Orge', 'crop_type_Blé', 'crop_type_Maïs',
    'sowing_year', 'sowing_month', 'sowing_day',
    'harvest_year', 'harvest_month', 'harvest_day',
    'growth_duration', 'area', 'soil_type_argileux', 'soil_type_sableux',
    'latitude', 'longitude', 'yield_quality_Bonne', 'yield_quality_Excellente',
    'yield_quality_Moyenne', 'status_en_culture', 'status_terminé'
]

# Define expected columns based on feature names if available
if feature_names is not None:
    # Use feature names from the model
    expected_columns = feature_names
else:
    # Fallback to base columns plus engineered features
    expected_columns = base_expected_columns + [
        'area_squared', 'growth_duration_squared', 'area_x_growth', 'growing_days'
    ]

@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Log incoming request
        logger.info("Received prediction request")

        # Get JSON data from the request
        input_data = request.json
        logger.info(f"Input data: {input_data}")

        # Convert JSON to DataFrame
        df = pd.DataFrame([input_data])

        # Add missing columns with default value 0
        for col in base_expected_columns:
            if col not in df.columns:
                logger.warning(f"Missing column {col} in input, adding with default value 0")
                df[col] = 0

        # Ensure all columns are numeric
        for col in df.columns:
            try:
                df[col] = pd.to_numeric(df[col])
            except Exception as e:
                logger.error(f"Error converting column {col} to numeric: {e}")
                df[col] = 0

        # Create engineered features
        logger.info("Creating engineered features")
        df = create_engineered_features(df)

        # Choose the appropriate preprocessing method
        processed_input = preprocess_input_data(df)

        # Load the ONNX model
        try:
            onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
            logger.info(f"Loading model from: {onnx_model_path}")
            session = ort.InferenceSession(onnx_model_path)
        except Exception as e:
            logger.error(f"Error loading ONNX model: {e}")
            return jsonify({'error': 'Model loading failed', 'details': str(e)}), 500

        # Get input shape information
        input_name = session.get_inputs()[0].name
        input_shape = session.get_inputs()[0].shape
        logger.info(f"Model input name: {input_name}, shape: {input_shape}")

        # Make predictions
        logger.info(f"Processed input shape: {processed_input.shape}")
        input_dict = {input_name: processed_input}
        predictions = session.run(None, input_dict)

        # Extract and format the prediction result
        prediction_value = float(predictions[0][0])
        logger.info(f"Prediction result: {prediction_value}")

        # Return the prediction as JSON
        return jsonify({'predicted_yield': prediction_value})

    except Exception as e:
        # Log the full error with traceback
        logger.error(f"Error during prediction: {e}")
        logger.error(traceback.format_exc())
        # Return a proper error response
        return jsonify({
            'error': 'Prediction failed',
            'message': str(e),
            'type': type(e).__name__
        }), 500

def create_engineered_features(df):
    """Create engineered features similar to those in the training process"""
    # Create polynomial features
    if 'area' in df.columns:
        df['area_squared'] = df['area'] ** 2

    if 'growth_duration' in df.columns:
        df['growth_duration_squared'] = df['growth_duration'] ** 2

    # Create interaction features
    if 'area' in df.columns and 'growth_duration' in df.columns:
        df['area_x_growth'] = df['area'] * df['growth_duration']

    # Calculate growing days (if not already present)
    if 'growing_days' not in df.columns:
        if ('sowing_year' in df.columns and 'harvest_year' in df.columns and
            'sowing_month' in df.columns and 'harvest_month' in df.columns and
            'sowing_day' in df.columns and 'harvest_day' in df.columns):
            try:
                # Create date objects to calculate growing days
                sowing_dates = pd.to_datetime(
                    df[['sowing_year', 'sowing_month', 'sowing_day']].fillna(0).astype(int))
                harvest_dates = pd.to_datetime(
                    df[['harvest_year', 'harvest_month', 'harvest_day']].fillna(0).astype(int))
                df['growing_days'] = (harvest_dates - sowing_dates).dt.days
            except Exception as e:
                logger.error(f"Error calculating growing days: {e}")
                df['growing_days'] = df['growth_duration'] if 'growth_duration' in df.columns else 0
        else:
            df['growing_days'] = df['growth_duration'] if 'growth_duration' in df.columns else 0

    return df

def preprocess_input_data(df):
    """Preprocess input data to match model expectations"""
    # If we have a preprocessor, use it
    if preprocessor is not None:
        logger.info("Using preprocessor for input transformation")

        # Get the feature names expected by the preprocessor
        if hasattr(preprocessor, 'transformers_'):
            numeric_features = [col for col in preprocessor.transformers_[0][2] if col in df.columns]
            categorical_features = [col for col in preprocessor.transformers_[1][2] if col in df.columns]

            # Fill missing values
            for col in numeric_features:
                if col not in df.columns:
                    df[col] = 0

            for col in categorical_features:
                if col not in df.columns:
                    df[col] = 'unknown'

            # Select only the features used by the preprocessor
            input_df = df[numeric_features + categorical_features]

            # Transform the data
            try:
                transformed_data = preprocessor.transform(input_df)
                return transformed_data.astype(np.float32)
            except Exception as e:
                logger.error(f"Error using preprocessor: {e}")
                # Fall back to manual preprocessing
                logger.info("Falling back to manual preprocessing")

    # Manual preprocessing as fallback
    logger.info("Using manual preprocessing")
    # Check what columns are available in the expected columns
    available_columns = [col for col in expected_columns if col in df.columns]

    # If we have feature names, use them to order the columns
    if feature_names is not None:
        ordered_columns = []
        for feature in feature_names:
            if feature in df.columns:
                ordered_columns.append(feature)
            else:
                logger.warning(f"Missing feature {feature}, adding with default value 0")
                df[feature] = 0
                ordered_columns.append(feature)

        # Reorder columns to match expected feature order
        input_df = df[ordered_columns]
    else:
        # Use all available expected columns
        for col in expected_columns:
            if col not in df.columns:
                logger.warning(f"Missing column {col}, adding with default value 0")
                df[col] = 0

        input_df = df[expected_columns]

    # Convert to numpy array
    X = input_df.values.astype(np.float32)

    return X

@app.route('/health', methods=['GET'])
def health_check():
    """Simple endpoint to verify the API is running"""
    return jsonify({'status': 'ok'})

@app.route('/model_info', methods=['GET'])
def model_info():
    """Endpoint to get information about the model"""
    # Load the model metadata if available
    try:
        metadata_path = os.path.join('prediction', 'model_metadata.joblib')
        if os.path.exists(metadata_path):
            metadata = joblib.load(metadata_path)
            return jsonify(metadata)
        else:
            # Return basic info
            return jsonify({
                'features': expected_columns,
                'feature_count': len(expected_columns),
                'message': 'Detailed metadata not available'
            })
    except Exception as e:
        return jsonify({
            'error': str(e),
            'features': expected_columns,
            'feature_count': len(expected_columns)
        })

if __name__ == '__main__':
    # Create prediction directory if it doesn't exist
    os.makedirs('prediction', exist_ok=True)

    # Check if model exists before starting server
    onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
    if not os.path.exists(onnx_model_path):
        logger.error(f"Model file not found at {onnx_model_path}")
        print(f"ERROR: Model file not found at {onnx_model_path}")
    else:
        logger.info(f"Model file found at {onnx_model_path}")
        app.run(host='0.0.0.0', port=5000, debug=True)