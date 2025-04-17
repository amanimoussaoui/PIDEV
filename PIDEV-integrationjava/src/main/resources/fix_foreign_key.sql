-- Supprimer l'ancienne contrainte de clé étrangère
ALTER TABLE reservation
DROP FOREIGN KEY fk_reservation_machine;

-- Ajouter la nouvelle contrainte avec ON DELETE CASCADE
ALTER TABLE reservation
ADD CONSTRAINT fk_reservation_machine
FOREIGN KEY (machine_id) REFERENCES machine(id)
ON DELETE CASCADE
ON UPDATE CASCADE; 