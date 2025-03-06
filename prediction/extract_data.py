# extract_data.py

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
    r.quantite AS yield_quantity,  -- Ensure this column is included
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

# 🟢 One-hot encoding des variables catégoriques
df = pd.get_dummies(df, columns=['crop_type', 'soil_type', 'yield_quality', 'status'], prefix=['crop_type', 'soil_type', 'yield_quality', 'status'])

# Réorganiser les colonnes selon l’ordre attendu par le modèle ONNX
expected_columns = [
    'culture_id', 'crop_type_Orge', 'crop_type_Blé', 'crop_type_Maïs', 
    'sowing_year', 'sowing_month', 'sowing_day', 
    'harvest_year', 'harvest_month', 'harvest_day', 
    'growth_duration', 'area', 'soil_type_argileux', 'soil_type_sableux', 
    'latitude', 'longitude', 'yield_quality_Bonne', 'yield_quality_Excellente', 
    'yield_quality_Moyenne', 'status_en_culture', 'status_terminé', 'yield_quantity'  # Include yield_quantity
]

# Ajouter les colonnes manquantes si elles n'existent pas
for col in expected_columns:
    if col not in df.columns:
        df[col] = 0  # Ajouter une colonne avec des zéros par défaut

# Réordonner les colonnes
df = df[expected_columns]

# Convertir en float (comme demandé par ONNX)
df = df.astype(float)

# Enregistrer dans un CSV
csv_path = os.path.join('prediction', 'agricultural_data.csv')
df.to_csv(csv_path, index=False)
print(f"✅ Données transformées et enregistrées dans {csv_path}")