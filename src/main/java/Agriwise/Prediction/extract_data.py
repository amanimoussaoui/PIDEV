import pandas as pd
import numpy as np
from sqlalchemy import create_engine
import os
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
import joblib
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
from sklearn.impute import SimpleImputer

# Create prediction directory if it doesn't exist
Path("prediction").mkdir(exist_ok=True)

# Database connection details
db_url = "mysql+mysqlconnector://root:@127.0.0.1:3306/agriwise"
engine = create_engine(db_url)

# SQL query
query = """
SELECT 
    c.id AS culture_id, 
    c.nom_culture AS crop_type, 
    c.date_semis AS sowing_date, 
    c.duree AS growth_duration, 
    c.statut AS status,
    p.superficie AS area, 
    p.type_sol AS soil_type, 
    p.latitude, 
    p.longitude,
    r.date_recolte AS harvest_date, 
    r.quantite AS yield_quantity,
    r.qualite AS yield_quality
FROM 
    Recolte r
JOIN 
    Culture c ON r.culture_id = c.id
JOIN 
    Parcelle p ON c.parcelle_id = p.id;
"""

# Load data
print("🔍 Loading data from database...")
df = pd.read_sql(query, engine)

# Check for missing values
print("\n🔍 Checking for missing values:")
missing_values = df.isnull().sum()
print(missing_values[missing_values > 0])

# Fill missing values
print("\n🔧 Handling missing values...")
# Use simple imputation for numeric columns
numeric_imputer = SimpleImputer(strategy='median')
categorical_imputer = SimpleImputer(strategy='most_frequent')

df['growth_duration'] = numeric_imputer.fit_transform(df[['growth_duration']])[:, 0]
df['area'] = numeric_imputer.fit_transform(df[['area']])[:, 0]
df['latitude'] = numeric_imputer.fit_transform(df[['latitude']])[:, 0]
df['longitude'] = numeric_imputer.fit_transform(df[['longitude']])[:, 0]
df['yield_quantity'] = numeric_imputer.fit_transform(df[['yield_quantity']])[:, 0]

# Detect and handle outliers
print("\n🔍 Detecting outliers in yield_quantity...")
Q1 = df['yield_quantity'].quantile(0.25)
Q3 = df['yield_quantity'].quantile(0.75)
IQR = Q3 - Q1
lower_bound = Q1 - 1.5 * IQR
upper_bound = Q3 + 1.5 * IQR

outliers = df[(df['yield_quantity'] < lower_bound) | (df['yield_quantity'] > upper_bound)]
print(f"Found {len(outliers)} outliers in yield_quantity")

# Cap outliers instead of removing them
df['yield_quantity'] = np.where(df['yield_quantity'] > upper_bound, upper_bound, df['yield_quantity'])
df['yield_quantity'] = np.where(df['yield_quantity'] < lower_bound, lower_bound, df['yield_quantity'])

# Extract date features
print("\n🔧 Extracting date features...")
df['sowing_date'] = pd.to_datetime(df['sowing_date'])
df['harvest_date'] = pd.to_datetime(df['harvest_date'])

# Calculate growing season length
df['growing_days'] = (df['harvest_date'] - df['sowing_date']).dt.days

# Extract date components
for date_col in ['sowing_date', 'harvest_date']:
    df[f'{date_col[:-5]}_year'] = df[date_col].dt.year
    df[f'{date_col[:-5]}_month'] = df[date_col].dt.month
    df[f'{date_col[:-5]}_day'] = df[date_col].dt.day

# Drop original date columns
df.drop(columns=['sowing_date', 'harvest_date'], inplace=True)

# Perform exploratory data analysis
print("\n📊 Performing exploratory data analysis...")

# Create EDA plots directory
eda_path = Path("prediction/eda")
eda_path.mkdir(exist_ok=True)

# Plot distributions of numeric features
numeric_features = ['growth_duration', 'area', 'latitude', 'longitude', 'yield_quantity', 'growing_days']
fig, axes = plt.subplots(len(numeric_features), 1, figsize=(10, 15))
for i, feature in enumerate(numeric_features):
    sns.histplot(df[feature], ax=axes[i])
    axes[i].set_title(f'Distribution of {feature}')
fig.tight_layout()
fig.savefig(os.path.join(eda_path, 'numeric_distributions.png'))

# Plot correlation matrix
plt.figure(figsize=(12, 10))
correlation_matrix = df[numeric_features].corr()
sns.heatmap(correlation_matrix, annot=True, cmap='coolwarm')
plt.title('Correlation Matrix of Numeric Features')
plt.tight_layout()
plt.savefig(os.path.join(eda_path, 'correlation_matrix.png'))

# Analyze relationship with target
features_to_plot = [f for f in numeric_features if f != 'yield_quantity']
fig, axes = plt.subplots(len(features_to_plot), 1, figsize=(10, 15))
for i, feature in enumerate(features_to_plot):
    sns.scatterplot(x=feature, y='yield_quantity', data=df, ax=axes[i])
    axes[i].set_title(f'{feature} vs yield_quantity')
fig.tight_layout()
fig.savefig(os.path.join(eda_path, 'feature_vs_target.png'))

# Categorical feature analysis
categorical_features = ['crop_type', 'soil_type', 'yield_quality', 'status']
fig, axes = plt.subplots(len(categorical_features), 1, figsize=(12, 15))
for i, feature in enumerate(categorical_features):
    sns.boxplot(x=feature, y='yield_quantity', data=df, ax=axes[i])
    axes[i].set_title(f'yield_quantity by {feature}')
    plt.xticks(rotation=45)
fig.tight_layout()
fig.savefig(os.path.join(eda_path, 'categorical_analysis.png'))

# Feature Engineering
print("\n🔧 Performing feature engineering...")

# Create polynomial features for relevant numeric columns
df['area_squared'] = df['area'] ** 2
df['growth_duration_squared'] = df['growth_duration'] ** 2

# Create interaction features that might be meaningful
df['area_x_growth'] = df['area'] * df['growth_duration']

# Define feature preprocessing
print("\n🔧 Setting up preprocessing pipeline...")

# Define categorical and numeric features
categorical_features = ['crop_type', 'soil_type', 'yield_quality', 'status']
numeric_features = ['growth_duration', 'growth_duration_squared', 'area', 'area_squared', 
                   'latitude', 'longitude', 'sowing_year', 'sowing_month', 'sowing_day',
                   'harvest_year', 'harvest_month', 'harvest_day', 'growing_days', 'area_x_growth']

# Define preprocessing for numerical features
numeric_transformer = Pipeline(steps=[
    ('imputer', SimpleImputer(strategy='median')),  # Handle any remaining missing values
    ('scaler', StandardScaler())  # Standardize features
])

# Define preprocessing for categorical features
categorical_transformer = Pipeline(steps=[
    ('imputer', SimpleImputer(strategy='most_frequent')),  # Handle any remaining missing values
    ('onehot', OneHotEncoder(handle_unknown='ignore', sparse_output=False))  # One-hot encode
])

# Combine preprocessing steps
preprocessor = ColumnTransformer(
    transformers=[
        ('num', numeric_transformer, numeric_features),
        ('cat', categorical_transformer, categorical_features)
    ])

# Save the original column order for reference
all_columns = numeric_features + categorical_features + ['yield_quantity']
df_for_model = df[all_columns].copy()

# Fit the preprocessor and save it
print("\n💾 Fitting and saving preprocessor...")
preprocessor.fit(df_for_model.drop(columns=['yield_quantity']))
joblib.dump(preprocessor, os.path.join('prediction', 'preprocessor.joblib'))

# Get feature names after transformation (for reference)
cat_feature_names = preprocessor.transformers_[1][1]['onehot'].get_feature_names_out(categorical_features)
feature_names = numeric_features + list(cat_feature_names)
joblib.dump(feature_names, os.path.join('prediction', 'feature_names.joblib'))

# Save the original data for the model
print("\n💾 Saving processed data...")
df_for_model.to_csv(os.path.join('prediction', 'agricultural_data_original.csv'), index=False)

# Apply transformations
X = preprocessor.transform(df_for_model.drop(columns=['yield_quantity']))
y = df_for_model['yield_quantity']

# Save transformed data to a file
transformed_data = pd.DataFrame(X, columns=feature_names)
transformed_data['yield_quantity'] = y
transformed_data.to_csv(os.path.join('prediction', 'agricultural_data_transformed.csv'), index=False)

# Also save the simpler one-hot encoded version (without scaling) for easier inspection
print("\n💾 Saving one-hot encoded data for inspection...")
df_encoded = pd.get_dummies(df_for_model, columns=categorical_features)
df_encoded.to_csv(os.path.join('prediction', 'agricultural_data.csv'), index=False)

print(f"\n✅ Data extraction, analysis, and preprocessing complete!")
print(f"✅ Files saved in the 'prediction' directory")
print(f"✅ EDA visualizations saved in the 'prediction/eda' directory")