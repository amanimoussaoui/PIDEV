-- Script de création de la table 'reservation'
-- À exécuter pour s'assurer que la table existe avec la bonne structure

CREATE TABLE IF NOT EXISTS `reservation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_machine` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'En attente',
  PRIMARY KEY (`id`),
  KEY `fk_reservation_machine` (`id_machine`),
  KEY `fk_reservation_user` (`id_user`),
  CONSTRAINT `fk_reservation_machine` FOREIGN KEY (`id_machine`) REFERENCES `machine` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 