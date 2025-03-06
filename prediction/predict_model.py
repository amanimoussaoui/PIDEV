# predict_model.py


import pandas as pd
import numpy as np
import onnxruntime as ort
import os

# Load the ONNX model
onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
session = ort.InferenceSession(onnx_model_path)

# Load the data from the CSV file inside the prediction folder
csv_path = os.path.join('prediction', 'agricultural_data.csv')
df = pd.read_csv(csv_path)

# Ensure the data has the same columns as expected by the model
expected_columns = [
    'culture_id', 'crop_type_Orge', 'crop_type_Blé', 'crop_type_Maïs', 'sowing_year', 
    'sowing_month', 'sowing_day', 'harvest_year', 'harvest_month', 'harvest_day', 
    'growth_duration', 'area', 'soil_type_argileux', 'soil_type_sableux',  
    'latitude', 'longitude', 'yield_quality_Bonne', 'yield_quality_Excellente', 'yield_quality_Moyenne', 
    'status_en_culture', 'status_terminé'
]  # ⚠️ Supprime 'soil_type_limoneux'


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

# The output is a list of arrays, where the first array contains the predictions
predictions = predictions[0]

# Add the predictions to the DataFrame
df['predicted_yield'] = predictions

# Save the predictions to a new CSV file
output_csv_path = os.path.join('prediction', 'agricultural_data_with_predictions.csv')
df.to_csv(output_csv_path, index=False)

print(f"✅ Predictions made and saved to {output_csv_path}")