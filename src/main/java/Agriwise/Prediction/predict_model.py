import pandas as pd
import numpy as np
import onnxruntime as ort
import os
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
import joblib
from datetime import datetime

# Create directories
Path("prediction/results").mkdir(exist_ok=True, parents=True)

# Load the ONNX model
print("🔍 Loading model and preprocessor...")
onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
session = ort.InferenceSession(onnx_model_path)

# Load the preprocessor
preprocessor = joblib.load(os.path.join('prediction', 'preprocessor.joblib'))
feature_names = joblib.load(os.path.join('prediction', 'feature_names.joblib'))

# Function to safely convert dates
def parse_date(date_str):
    try:
        return pd.to_datetime(date_str)
    except:
        return None

# Function to prepare input data with proper feature engineering
def prepare_input_data(df):
    print("🔧 Preparing input data...")
    
    # Make a copy to avoid modifying the original
    df = df.copy()
    
    # Handle date columns if they exist
    date_columns = ['sowing_date', 'harvest_date']
    date_components = {'sowing': [], 'harvest': []}
    
    for date_prefix in ['sowing', 'harvest']:
        date_col = f'{date_prefix}_date'
        
        # Check if we have the date as a single column or as components
        if date_col in df.columns:
            df[date_col] = df[date_col].apply(parse_date)
            # Extract date components
            df[f'{date_prefix}_year'] = df[date_col].dt.year
            df[f'{date_prefix}_month'] = df[date_col].dt.month
            df[f'{date_prefix}_day'] = df[date_col].dt.day
            df.drop(date_col, axis=1, inplace=True)
        
        # Check if we already have the components
        year_col = f'{date_prefix}_year'
        month_col = f'{date_prefix}_month'
        day_col = f'{date_prefix}_day'
        
        if all(col in df.columns for col in [year_col, month_col, day_col]):
            # Ensure components are numeric
            for col in [year_col, month_col, day_col]:
                df[col] = pd.to_numeric(df[col], errors='coerce')
    
    # Calculate growing days if both dates exist
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
            print(f"Warning: Unable to calculate growing days. Error: {e}")
            if 'growth_duration' in df.columns:
                df['growing_days'] = df['growth_duration']
            else:
                df['growing_days'] = np.nan
    elif 'growth_duration' in df.columns:
        df['growing_days'] = df['growth_duration']
    
    # Create polynomial features
    if 'area' in df.columns:
        df['area_squared'] = df['area'] ** 2
    
    if 'growth_duration' in df.columns:
        df['growth_duration_squared'] = df['growth_duration'] ** 2
    
    # Create interaction features
    if 'area' in df.columns and 'growth_duration' in df.columns:
        df['area_x_growth'] = df['area'] * df['growth_duration']
    
    # Keep track of record IDs if they exist
    id_cols = [col for col in df.columns if col.endswith('_id')]
    record_ids = df[id_cols].copy() if id_cols else None
    
    # Apply the preprocessor
    print("🔧 Applying preprocessing transformations...")
    
    # Get expected columns from preprocessor
    numeric_cols = preprocessor.transformers_[0][2]
    categorical_cols = preprocessor.transformers_[1][2]
    
    # Check and add missing columns with default values
    for col in numeric_cols:
        if col not in df.columns:
            print(f"Warning: Missing numeric column {col}, adding with default value 0")
            df[col] = 0
    
    for col in categorical_cols:
        if col not in df.columns:
            print(f"Warning: Missing categorical column {col}, adding with default value 'unknown'")
            df[col] = 'unknown'
    
    # Ensure all columns expected by the preprocessor are present
    input_columns = numeric_cols + categorical_cols
    input_df = df[input_columns].copy()
    
    # Transform the data
    transformed_data = preprocessor.transform(input_df)
    
    return transformed_data, record_ids

# Load the data
print("🔍 Loading agricultural data...")
try:
    # First try to load the original data
    csv_path = os.path.join('prediction', 'agricultural_data_original.csv')
    df = pd.read_csv(csv_path)
    print(f"Loaded original data from {csv_path}")
except FileNotFoundError:
    # Fall back to the encoded data
    csv_path = os.path.join('prediction', 'agricultural_data.csv')
    df = pd.read_csv(csv_path)
    print(f"Loaded encoded data from {csv_path}")

# Check if yield_quantity is present (we'll need to remove it for prediction)
has_yield = 'yield_quantity' in df.columns
if has_yield:
    print("Found yield_quantity column. This will be used for comparison with predictions.")
    actual_yield = df['yield_quantity'].copy()
    df_for_prediction = df.drop(columns=['yield_quantity'])
else:
    print("No yield_quantity column found. Generating predictions only.")
    df_for_prediction = df.copy()

# Prepare the input data
X, record_ids = prepare_input_data(df_for_prediction)

# Make predictions
print("🔍 Making predictions...")
input_name = session.get_inputs()[0].name
input_dict = {input_name: X.astype(np.float32)}
predictions = session.run(None, input_dict)[0]

# Combine predictions with record IDs if available
results = pd.DataFrame({'predicted_yield': predictions.flatten()})
if record_ids is not None:
    results = pd.concat([record_ids, results], axis=1)

# Add actual yield if available
if has_yield:
    results['actual_yield'] = actual_yield
    results['error'] = results['actual_yield'] - results['predicted_yield']
    results['abs_error'] = abs(results['error'])
    results['error_percentage'] = abs(results['error'] / results['actual_yield'] * 100)
    
    # Calculate overall metrics
    mae = results['abs_error'].mean()
    mape = results['error_percentage'].mean()
    
    print(f"\nPrediction Metrics:")
    print(f"  Mean Absolute Error (MAE): {mae:.2f}")
    print(f"  Mean Absolute Percentage Error (MAPE): {mape:.2f}%")
    
    # Create visualizations
    plt.figure(figsize=(10, 6))
    plt.scatter(results['actual_yield'], results['predicted_yield'], alpha=0.5)
    plt.plot([results['actual_yield'].min(), results['actual_yield'].max()], 
             [results['actual_yield'].min(), results['actual_yield'].max()], 'r--')
    plt.xlabel('Actual Yield')
    plt.ylabel('Predicted Yield')
    plt.title('Actual vs Predicted Yield')
    plt.tight_layout()
    plt.savefig(os.path.join('prediction', 'results', 'prediction_comparison.png'))
    
    # Error distribution
    plt.figure(figsize=(10, 6))
    plt.hist(results['error'], bins=20)
    plt.xlabel('Prediction Error')
    plt.ylabel('Frequency')
    plt.title('Distribution of Prediction Errors')
    plt.axvline(x=0, color='r', linestyle='--')
    plt.tight_layout()
    plt.savefig(os.path.join('prediction', 'results', 'error_distribution.png'))
    
    # Identify records with large errors
    large_errors = results.sort_values('abs_error', ascending=False).head(10)
    print("\nRecords with largest prediction errors:")
    print(large_errors)

# Save results
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
output_path = os.path.join('prediction', 'results', f'predictions_{timestamp}.csv')
results.to_csv(output_path, index=False)

print(f"\n✅ Predictions complete!")
print(f"✅ Results saved to {output_path}")
if has_yield:
    print(f"✅ Visualizations saved in 'prediction/results' directory")