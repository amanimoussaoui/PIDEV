CREATE TABLE IF NOT EXISTS reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    machine_id INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(50) NOT NULL,
    prix_total DOUBLE NOT NULL,
    FOREIGN KEY (machine_id) REFERENCES machine(id)
); 