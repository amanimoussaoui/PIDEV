-- Script pour corriger la structure de la table reservation
-- À exécuter pour renommer la colonne id_machine en machine_id

-- Vérifier si la table existe déjà
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'agriwise' AND table_name = 'reservation');

-- Si la table existe, modifier la colonne
DELIMITER //
CREATE PROCEDURE fix_reservation_table()
BEGIN
    IF @table_exists > 0 THEN
        -- Vérifier si la colonne id_machine existe
        SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'agriwise' AND table_name = 'reservation' AND column_name = 'id_machine');
        
        -- Renommer la colonne seulement si elle existe
        IF @column_exists > 0 THEN
            ALTER TABLE `agriwise`.`reservation` 
            CHANGE COLUMN `id_machine` `machine_id` INT(11) NOT NULL;
        END IF;
    ELSE
        -- Créer la table avec la bonne structure si elle n'existe pas
        CREATE TABLE `agriwise`.`reservation` (
          `id` int(11) NOT NULL AUTO_INCREMENT,
          `machine_id` int(11) NOT NULL,
          `id_user` int(11) NOT NULL,
          `date_debut` date NOT NULL,
          `date_fin` date NOT NULL,
          `status` varchar(50) NOT NULL DEFAULT 'En attente',
          PRIMARY KEY (`id`),
          KEY `fk_reservation_machine` (`machine_id`),
          KEY `fk_reservation_user` (`id_user`),
          CONSTRAINT `fk_reservation_machine` FOREIGN KEY (`machine_id`) REFERENCES `machine` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    END IF;
END //
DELIMITER ;

-- Exécuter la procédure
CALL fix_reservation_table();

-- Supprimer la procédure temporaire
DROP PROCEDURE IF EXISTS fix_reservation_table; 