-- Script pour corriger les noms de colonnes dans la table reservation

-- Vérifier si la table existe
SET @table_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = 'agriwise' 
    AND table_name = 'reservation'
);

DELIMITER //
CREATE PROCEDURE fix_reservation_table()
BEGIN
    IF @table_exists > 0 THEN
        -- 1. Vérifier si la colonne id_machine_id existe, sinon renommer id_machine en id_machine_id
        SET @has_id_machine = (
            SELECT COUNT(*) 
            FROM information_schema.columns 
            WHERE table_schema = 'agriwise' 
            AND table_name = 'reservation' 
            AND column_name = 'id_machine'
        );
        
        SET @has_id_machine_id = (
            SELECT COUNT(*) 
            FROM information_schema.columns 
            WHERE table_schema = 'agriwise' 
            AND table_name = 'reservation' 
            AND column_name = 'id_machine_id'
        );
        
        IF @has_id_machine > 0 AND @has_id_machine_id = 0 THEN
            -- Renommer id_machine en id_machine_id
            ALTER TABLE `agriwise`.`reservation` 
            CHANGE COLUMN `id_machine` `id_machine_id` INT(11) NOT NULL;
        END IF;
        
        -- 2. Vérifier si la colonne user_id existe, sinon renommer id_user en user_id
        SET @has_id_user = (
            SELECT COUNT(*) 
            FROM information_schema.columns 
            WHERE table_schema = 'agriwise' 
            AND table_name = 'reservation' 
            AND column_name = 'id_user'
        );
        
        SET @has_user_id = (
            SELECT COUNT(*) 
            FROM information_schema.columns 
            WHERE table_schema = 'agriwise' 
            AND table_name = 'reservation' 
            AND column_name = 'user_id'
        );
        
        IF @has_id_user > 0 AND @has_user_id = 0 THEN
            -- Renommer id_user en user_id
            ALTER TABLE `agriwise`.`reservation` 
            CHANGE COLUMN `id_user` `user_id` INT(11) NOT NULL;
        END IF;
        
    ELSE
        -- Créer la table avec la structure correcte
        CREATE TABLE `agriwise`.`reservation` (
          `id` int(11) NOT NULL AUTO_INCREMENT,
          `id_machine_id` int(11) NOT NULL,
          `user_id` int(11) NOT NULL,
          `date_debut` date NOT NULL,
          `date_fin` date NOT NULL,
          PRIMARY KEY (`id`),
          KEY `fk_reservation_machine` (`id_machine_id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    END IF;
END //
DELIMITER ;

-- Exécuter la procédure
CALL fix_reservation_table();

-- Supprimer la procédure temporaire
DROP PROCEDURE IF EXISTS fix_reservation_table; 