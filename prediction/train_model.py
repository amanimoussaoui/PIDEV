# train_model.py

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import os

# Load the data from the CSV file inside the prediction folder
csv_path = os.path.join('prediction', 'agricultural_data.csv')
df = pd.read_csv(csv_path)

# Display the first few rows
print(df.head())

# Check for missing values and drop them
print(df.isnull().sum())
df.dropna(inplace=True)

# Define expected columns
expected_columns = [
    'culture_id', 'crop_type_Orge', 'crop_type_Blé', 'crop_type_Maïs', 
    'sowing_year', 'sowing_month', 'sowing_day', 
    'harvest_year', 'harvest_month', 'harvest_day', 
    'growth_duration', 'area', 'soil_type_argileux', 'soil_type_sableux', 
    'latitude', 'longitude', 'yield_quality_Bonne', 'yield_quality_Excellente', 
    'yield_quality_Moyenne', 'status_en_culture', 'status_terminé', 'yield_quantity'
]

# Ensure all expected columns exist
for col in expected_columns:
    if col not in df.columns:
        df[col] = 0  # Fill missing columns with 0

# Keep only the relevant columns
df = df[expected_columns]

# Features and target
X = df.drop(columns=['yield_quantity'])  # Features
y = df['yield_quantity']  # Target

# Split the data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Initialize the model
model = RandomForestRegressor(n_estimators=100, random_state=42)

# Train the model
model.fit(X_train, y_train)

# Make predictions
predictions = model.predict(X_test)

# Evaluate the model
mae = mean_absolute_error(y_test, predictions)
print(f'Mean Absolute Error: {mae}')

# Convert the model to ONNX format
initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]  # Define input type
onnx_model = convert_sklearn(model, initial_types=initial_type)

# Save the ONNX model to a file
onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
with open(onnx_model_path, "wb") as f:
    f.write(onnx_model.SerializeToString())

print(f"✅ ONNX model saved to {onnx_model_path}")