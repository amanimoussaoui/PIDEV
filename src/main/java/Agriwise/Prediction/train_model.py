import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split, GridSearchCV, cross_val_score, KFold
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.linear_model import LinearRegression, Ridge
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import os
import joblib
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path

# Create model directory if it doesn't exist
Path("prediction/models").mkdir(exist_ok=True, parents=True)

# Load preprocessed data and preprocessor
print("🔍 Loading data and preprocessor...")
preprocessor = joblib.load(os.path.join('prediction', 'preprocessor.joblib'))
feature_names = joblib.load(os.path.join('prediction', 'feature_names.joblib'))
df_original = pd.read_csv(os.path.join('prediction', 'agricultural_data_original.csv'))
df_transformed = pd.read_csv(os.path.join('prediction', 'agricultural_data_transformed.csv'))

# Prepare features and target
X = df_transformed.drop(columns=['yield_quantity'])
y = df_transformed['yield_quantity']

# Feature importance analysis function
def plot_feature_importance(model, feature_names, output_path):
    plt.figure(figsize=(12, 8))
    importances = pd.DataFrame({
        'Feature': feature_names,
        'Importance': model.feature_importances_
    }).sort_values(by='Importance', ascending=False)
    
    sns.barplot(x='Importance', y='Feature', data=importances.head(20))
    plt.title('Feature Importances')
    plt.tight_layout()
    plt.savefig(output_path)
    return importances

# Split the data into training, validation, and test sets
print("\n🔧 Splitting data into train, validation, and test sets...")
X_train, X_temp, y_train, y_temp = train_test_split(X, y, test_size=0.3, random_state=42)
X_val, X_test, y_val, y_test = train_test_split(X_temp, y_temp, test_size=0.5, random_state=42)

print(f"Training set size: {X_train.shape[0]}")
print(f"Validation set size: {X_val.shape[0]}")
print(f"Test set size: {X_test.shape[0]}")

# Define evaluation metrics
def evaluate_model(model, X, y, model_name="Model"):
    y_pred = model.predict(X)
    mae = mean_absolute_error(y, y_pred)
    mse = mean_squared_error(y, y_pred)
    rmse = np.sqrt(mse)
    r2 = r2_score(y, y_pred)
    
    print(f"{model_name} Performance:")
    print(f"  MAE: {mae:.2f}")
    print(f"  MSE: {mse:.2f}")
    print(f"  RMSE: {rmse:.2f}")
    print(f"  R²: {r2:.4f}")
    
    return {'mae': mae, 'mse': mse, 'rmse': rmse, 'r2': r2}

# Train and evaluate multiple models
print("\n🔧 Training and comparing multiple baseline models...")
models = {
    'Linear Regression': LinearRegression(),
    'Ridge Regression': Ridge(alpha=1.0),
    'Random Forest': RandomForestRegressor(n_estimators=100, random_state=42),
    'Gradient Boosting': GradientBoostingRegressor(n_estimators=100, random_state=42)
}

# Cross-validation for each model
cv_results = {}
for name, model in models.items():
    print(f"\nPerforming cross-validation for {name}...")
    cv = KFold(n_splits=5, shuffle=True, random_state=42)
    cv_scores = cross_val_score(model, X_train, y_train, cv=cv, scoring='neg_mean_absolute_error')
    cv_mae = -cv_scores.mean()
    cv_results[name] = cv_mae
    print(f"Cross-validation MAE: {cv_mae:.2f}")

# Train each model on the full training set and evaluate
model_results = {}
for name, model in models.items():
    print(f"\nTraining {name}...")
    model.fit(X_train, y_train)
    
    # Evaluate on validation set
    results = evaluate_model(model, X_val, y_val, f"{name} (Validation)")
    model_results[name] = results
    
    # Save the model
    joblib.dump(model, os.path.join('prediction', 'models', f'{name.lower().replace(" ", "_")}.joblib'))

# Identify the best model based on validation MAE
best_model_name = min(model_results, key=lambda k: model_results[k]['mae'])
print(f"\n✅ Best model based on validation MAE: {best_model_name}")

# Hyperparameter tuning for the best model
print(f"\n🔧 Performing hyperparameter tuning for {best_model_name}...")

if best_model_name == 'Random Forest':
    param_grid = {
        'n_estimators': [50, 100, 200],
        'max_depth': [None, 10, 20, 30],
        'min_samples_split': [2, 5, 10],
        'min_samples_leaf': [1, 2, 4]
    }
    base_model = RandomForestRegressor(random_state=42)
elif best_model_name == 'Gradient Boosting':
    param_grid = {
        'n_estimators': [50, 100, 200],
        'learning_rate': [0.01, 0.1, 0.2],
        'max_depth': [3, 5, 7],
        'subsample': [0.8, 0.9, 1.0]
    }
    base_model = GradientBoostingRegressor(random_state=42)
elif best_model_name == 'Ridge Regression':
    param_grid = {
        'alpha': [0.1, 1.0, 10.0, 100.0]
    }
    base_model = Ridge(random_state=42)
else:  # Linear Regression has no hyperparameters to tune
    print("Linear Regression has no hyperparameters to tune. Skipping tuning step.")
    best_model = models[best_model_name]
    tuned = False

# Perform grid search if we have hyperparameters to tune
if best_model_name != 'Linear Regression':
    grid_search = GridSearchCV(
        base_model, param_grid, cv=5,
        scoring='neg_mean_absolute_error', n_jobs=-1, verbose=1
    )
    grid_search.fit(X_train, y_train)
    
    print(f"Best parameters: {grid_search.best_params_}")
    best_model = grid_search.best_estimator_
    tuned = True
    
    # Save tuned model
    joblib.dump(best_model, os.path.join('prediction', 'models', f'{best_model_name.lower().replace(" ", "_")}_tuned.joblib'))

# Evaluate the best model on the test set
print("\n🔍 Evaluating best model on test set...")
if tuned:
    final_metrics = evaluate_model(best_model, X_test, y_test, f"{best_model_name} (Test)")
else:
    final_metrics = evaluate_model(models[best_model_name], X_test, y_test, f"{best_model_name} (Test)")

# Plot feature importance if the model supports it
if hasattr(best_model, 'feature_importances_'):
    print("\n📊 Plotting feature importance...")
    importances = plot_feature_importance(
        best_model, 
        feature_names, 
        os.path.join('prediction', 'feature_importance.png')
    )
    print("Top 10 important features:")
    print(importances.head(10))

# Visualize predictions vs actual values
print("\n📊 Visualizing predictions vs actual values...")
if tuned:
    y_pred = best_model.predict(X_test)
else:
    y_pred = models[best_model_name].predict(X_test)

plt.figure(figsize=(10, 6))
plt.scatter(y_test, y_pred, alpha=0.5)
plt.plot([y_test.min(), y_test.max()], [y_test.min(), y_test.max()], 'r--')
plt.xlabel('Actual Yield')
plt.ylabel('Predicted Yield')
plt.title('Actual vs Predicted Yield')
plt.tight_layout()
plt.savefig(os.path.join('prediction', 'actual_vs_predicted.png'))

# Convert the best model to ONNX format
print("\n💾 Converting the best model to ONNX format...")
initial_type = [('float_input', FloatTensorType([None, X.shape[1]]))]

# Select the model to convert (tuned if available)
if tuned:
    model_to_convert = best_model
else:
    model_to_convert = models[best_model_name]

# Convert and save
onnx_model = convert_sklearn(model_to_convert, initial_types=initial_type)
onnx_model_path = os.path.join('prediction', 'crop_yield_model.onnx')
with open(onnx_model_path, "wb") as f:
    f.write(onnx_model.SerializeToString())

# Save metadata about the model and features
model_metadata = {
    'model_type': best_model_name,
    'tuned': tuned,
    'metrics': final_metrics,
    'feature_count': X.shape[1],
    'training_samples': X_train.shape[0],
    'validation_samples': X_val.shape[0],
    'test_samples': X_test.shape[0]
}

joblib.dump(model_metadata, os.path.join('prediction', 'model_metadata.joblib'))

print(f"\n✅ Model training and evaluation complete!")
print(f"✅ Best model: {best_model_name}")
print(f"✅ Test MAE: {final_metrics['mae']:.2f}")
print(f"✅ ONNX model saved to {onnx_model_path}")