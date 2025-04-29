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

# Get the expected columns from the training data (exclude the target column 'yield_quantity')
expected_columns = df.drop(columns=['yield_quantity']).columns.tolist()

# Debug: Print expected columns
print("Expected columns:")
print(expected_columns)
print(f"Number of expected columns: {len(expected_columns)}")

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