-- Script pour ajouter une colonne status à la table reservation
-- Cette colonne peut contenir une valeur NULL

-- Vérifier si la colonne existe déjà
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.columns 
    WHERE table_schema = 'agriwise' 
    AND table_name = 'reservation' 
    AND column_name = 'status'
);

-- Ajouter la colonne seulement si elle n'existe pas
DELIMITER //
CREATE PROCEDURE add_status_column()
BEGIN
    IF @column_exists = 0 THEN
        ALTER TABLE `agriwise`.`reservation` 
        ADD COLUMN `status` VARCHAR(50) NULL COMMENT 'Statut de la réservation (En attente, Confirmée, Annulée, Terminée)' AFTER `date_fin`;
        
        -- Mettre à jour les lignes existantes avec une valeur par défaut
        UPDATE `agriwise`.`reservation` SET `status` = 'En attente' WHERE `status` IS NULL;
    END IF;
END //
DELIMITER ;

-- Exécuter la procédure
CALL add_status_column();

-- Supprimer la procédure temporaire
DROP PROCEDURE IF EXISTS add_status_column; 