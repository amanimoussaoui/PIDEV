import pandas as pd
from sqlalchemy import create_engine
import os

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
df = pd.read_sql(query, engine)

# 🟢 Convertir les dates en colonnes séparées (Année, Mois, Jour)
df[['sowing_year', 'sowing_month', 'sowing_day']] = df['sowing_date'].astype(str).str.split('-', expand=True)
df[['harvest_year', 'harvest_month', 'harvest_day']] = df['harvest_date'].astype(str).str.split('-', expand=True)

# Supprimer les anciennes colonnes de date
df.drop(columns=['sowing_date', 'harvest_date'], inplace=True)

# 🟢 Get unique values for categorical columns BEFORE one-hot encoding
crop_types = df['crop_type'].unique()  # Get all unique crop types
soil_types = df['soil_type'].unique()  # Get all unique soil types
yield_qualities = df['yield_quality'].unique()  # Get all unique yield qualities
statuses = df['status'].unique()  # Get all unique statuses

# 🟢 One-hot encoding des variables catégoriques
df = pd.get_dummies(df, columns=['crop_type', 'soil_type', 'yield_quality', 'status'], prefix=['crop_type', 'soil_type', 'yield_quality', 'status'])

# Create expected columns dynamically
expected_columns = [
    'culture_id',
    *[f'crop_type_{crop}' for crop in crop_types],  # Add all crop types
    'sowing_year', 'sowing_month', 'sowing_day',
    'harvest_year', 'harvest_month', 'harvest_day',
    'growth_duration', 'area',
    *[f'soil_type_{soil}' for soil in soil_types],  # Add all soil types
    'latitude', 'longitude',
    *[f'yield_quality_{quality}' for quality in yield_qualities],  # Add all yield qualities
    *[f'status_{status}' for status in statuses],  # Add all statuses
    'yield_quantity'  # Include yield_quantity
]

# Add missing columns with default value 0
for col in expected_columns:
    if col not in df.columns:
        df[col] = 0

# Reorder columns
df = df[expected_columns]

# Convertir en float (comme demandé par ONNX)
df = df.astype(float)

# Enregistrer dans un CSV
csv_path = os.path.join('prediction', 'agricultural_data.csv')
df.to_csv(csv_path, index=False)
print(f"✅ Données transformées et enregistrées dans {csv_path}")