# predict_api.py

from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
import onnxruntime as ort
import os

# Initialize Flask app
app = Flask(__name__)

# Load the ONNX model
onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
session = ort.InferenceSession(onnx_model_path)

# Define the expected columns for the model
expected_columns = [
    'culture_id', 'crop_type_Orge', 'crop_type_Blé', 'crop_type_Maïs', 
    'sowing_year', 'sowing_month', 'sowing_day', 
    'harvest_year', 'harvest_month', 'harvest_day', 
    'growth_duration', 'area', 'soil_type_argileux', 'soil_type_sableux', 
    'latitude', 'longitude', 'yield_quality_Bonne', 'yield_quality_Excellente', 
    'yield_quality_Moyenne', 'status_en_culture', 'status_terminé'
]

@app.route('/predict', methods=['POST'])
def predict():
    # Get JSON data from the request
    input_data = request.json

    # Convert JSON to DataFrame
    df = pd.DataFrame([input_data])

    # Add missing columns with default value 0
    for col in expected_columns:
        if col not in df.columns:
            df[col] = 0

    # Reorder columns to match the expected input format
    df = df[expected_columns]

    # Convert the DataFrame to a numpy array of type float32
    input_data = df.astype(np.float32).values

    # Prepare the input for the ONNX model
    input_name = session.get_inputs()[0].name
    input_dict = {input_name: input_data}

    # Make predictions
    predictions = session.run(None, input_dict)

    # Return the prediction as JSON
    return jsonify({'predicted_yield': float(predictions[0][0])})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)