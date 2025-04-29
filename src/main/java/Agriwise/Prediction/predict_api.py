from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
import onnxruntime as ort
import os

# Initialize Flask app
app = Flask(__name__)

# Get the directory where this script is located
script_dir = os.path.dirname(os.path.abspath(__file__))

# Construct absolute paths
onnx_model_path = os.path.join(script_dir, 'crop_yield_model.onnx')
training_data_path = os.path.join(script_dir, 'agricultural_data.csv')

# Verify files exist
if not os.path.exists(onnx_model_path):
    raise FileNotFoundError(f"ONNX model not found at: {onnx_model_path}")

if not os.path.exists(training_data_path):
    raise FileNotFoundError(f"Training data not found at: {training_data_path}")

# Load the ONNX model
session = ort.InferenceSession(onnx_model_path)

# Load training data to get expected columns
training_data = pd.read_csv(training_data_path)
expected_columns = training_data.drop(columns=['yield_quantity']).columns.tolist()

print("Expected columns:", expected_columns)

def prepare_input_data(input_data):
    df = pd.DataFrame([input_data])

    # Add missing columns
    for col in expected_columns:
        if col not in df.columns:
            df[col] = 0

    return df[expected_columns].astype(np.float32).values

@app.route('/predict', methods=['POST'])
def predict():
    input_data = request.json
    print("Received:", input_data)

    try:
        input_array = prepare_input_data(input_data)
        input_name = session.get_inputs()[0].name
        prediction = session.run(None, {input_name: input_array})[0][0]
        return jsonify({'predicted_yield': float(prediction)})
    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)