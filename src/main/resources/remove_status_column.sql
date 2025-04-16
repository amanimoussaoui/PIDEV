-- Script pour supprimer la colonne status de la table reservation

-- Vérifier si la colonne existe
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.columns 
    WHERE table_schema = 'agriwise' 
    AND table_name = 'reservation' 
    AND column_name = 'status'
);

-- Supprimer la colonne seulement si elle existe
DELIMITER //
CREATE PROCEDURE remove_status_column()
BEGIN
    IF @column_exists > 0 THEN
        ALTER TABLE `agriwise`.`reservation` 
        DROP COLUMN `status`;
    END IF;
END //
DELIMITER ;

-- Exécuter la procédure
CALL remove_status_column();

-- Supprimer la procédure temporaire
DROP PROCEDURE IF EXISTS remove_status_column; 