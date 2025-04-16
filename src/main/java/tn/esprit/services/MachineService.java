package tn.esprit.services;

import tn.esprit.entities.Machine;
import tn.esprit.util.MaConnexion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MachineService implements IService<Machine> {
    
    private final Connection cnx;
    
    public MachineService() {
        this.cnx = MaConnexion.getInstance().getCon();
        try {
            fixForeignKeyConstraint();
        } catch (SQLException | IOException e) {
            System.err.println("Erreur lors de la mise à jour de la contrainte : " + e.getMessage());
        }
    }

    /**
     * Fixes the foreign key constraint for the machine table by executing SQL commands from fix_foreign_key.sql.
     * This method ensures that the proper CASCADE behavior is set up for foreign key relationships.
     *
     * @throws SQLException if there is an error executing the SQL commands
     * @throws IOException if there is an error reading the SQL file
     */
    private void fixForeignKeyConstraint() throws SQLException, IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/fix_foreign_key.sql"))))) {
            
            Connection connection = MaConnexion.getInstance().getCon();
            Statement statement = connection.createStatement();
            
            StringBuilder sqlCommands = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlCommands.append(line);
                if (line.trim().endsWith(";")) {
                    statement.execute(sqlCommands.toString());
                    sqlCommands.setLength(0);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing SQL commands: " + e.getMessage());
            throw e; // Propagate the exception
        } catch (IOException e) {
            System.err.println("Error reading SQL file: " + e.getMessage());
            throw e; // Propagate the exception
        }
    }

    public Machine readById(int id) throws SQLException {
        String query = "SELECT * FROM machine WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMachineFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    @Override
    public void create(Machine machine) throws SQLException {
        String query = "INSERT INTO machine (user_id, nom, description, prix, etat, disponibilite, date_maintenance, likes, dislikes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, machine.getId_user());
            ps.setString(2, machine.getNom());
            ps.setString(3, machine.getDescription());
            ps.setDouble(4, machine.getPrix());
            ps.setString(5, "Bon état"); // État par défaut
            ps.setString(6, "Disponible"); // Disponibilité par défaut
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now())); // Date maintenance actuelle
            ps.setInt(8, 0); // Initialiser likes à 0
            ps.setInt(9, 0); // Initialiser dislikes à 0
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                machine.setId(rs.getInt(1));
            }
            
            // Mettre à jour l'objet machine avec les valeurs par défaut
            machine.setEtat("Bon état");
            machine.setDisponibilite("Disponible");
            machine.setDateMaintenance(LocalDateTime.now());
            machine.setLikes(0);
            machine.setDislikes(0);
        }
    }
    
    @Override
    public void update(Machine machine) throws SQLException {
        String query = "UPDATE machine SET user_id = ?, nom = ?, description = ?, prix = ?, etat = ?, disponibilite = ?, date_maintenance = ?, likes = ?, dislikes = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, machine.getId_user());
            ps.setString(2, machine.getNom());
            ps.setString(3, machine.getDescription());
            ps.setDouble(4, machine.getPrix());
            ps.setString(5, machine.getEtat());
            ps.setString(6, machine.getDisponibilite());
            ps.setTimestamp(7, Timestamp.valueOf(machine.getDateMaintenance()));
            ps.setInt(8, machine.getLikes());
            ps.setInt(9, machine.getDislikes());
            ps.setInt(10, machine.getId());
            ps.executeUpdate();
        }
    }
    
    @Override
    public void delete(Machine machine) throws SQLException {

        String deleteReservationsQuery = "DELETE FROM reservation WHERE id_machine_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteReservationsQuery)) {
            ps.setInt(1, machine.getId());
            ps.executeUpdate();
        }

        // Then delete the machine
        String deleteMachineQuery = "DELETE FROM machine WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteMachineQuery)) {
            ps.setInt(1, machine.getId());
            ps.executeUpdate();
        }
    }
    
    @Override
    public List<Machine> readAll() throws SQLException {
        List<Machine> machines = new ArrayList<>();
        String query = "SELECT * FROM machine";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                machines.add(extractMachineFromResultSet(rs));
            }
        }
        return machines;
    }
    
    private Machine extractMachineFromResultSet(ResultSet rs) throws SQLException {
        Machine machine = new Machine();
        machine.setId(rs.getInt("id"));
        machine.setId_user(rs.getInt("user_id"));
        machine.setNom(rs.getString("nom"));
        machine.setDescription(rs.getString("description"));
        machine.setPrix(rs.getDouble("prix"));
        machine.setEtat(rs.getString("etat"));
        machine.setDisponibilite(rs.getString("disponibilite"));
        machine.setDateMaintenance(rs.getTimestamp("date_maintenance").toLocalDateTime());
        machine.setLikes(rs.getInt("likes"));
        machine.setDislikes(rs.getInt("dislikes"));
        return machine;
    }
    
    public List<Machine> searchMachines(String searchText) throws SQLException {
        List<Machine> machines = new ArrayList<>();
        String query = "SELECT * FROM machine WHERE LOWER(nom) LIKE ? OR LOWER(description) LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    machines.add(extractMachineFromResultSet(rs));
                }
            }
        }
        return machines;
    }
    
    public List<Machine> getAvailableMachines() throws SQLException {
        List<Machine> machines = new ArrayList<>();
        String query = "SELECT * FROM machine WHERE disponibilite = 'Disponible'";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                machines.add(extractMachineFromResultSet(rs));
            }
        }
        return machines;
    }

    public List<Machine> getMachinesByUserId(int userId) throws SQLException {
        List<Machine> machines = new ArrayList<>();
        String query = "SELECT * FROM machine WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    machines.add(extractMachineFromResultSet(rs));
                }
            }
        }
        return machines;
    }
}
