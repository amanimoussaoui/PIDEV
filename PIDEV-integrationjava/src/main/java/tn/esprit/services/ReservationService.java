package tn.esprit.services;

import tn.esprit.entities.Machine;
import tn.esprit.entities.Reservation;
import tn.esprit.util.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    private final Connection cnx;
    private final MachineService machineService;

    public ReservationService() {
        this.cnx = MaConnexion.getInstance().getCon();
        this.machineService = new MachineService();
    }

    @Override
    public void create(Reservation reservation) throws SQLException {
        // Vérifier si la machine est disponible
        Machine machine = machineService.readById(reservation.getId_machine_id());
        if (machine == null) {
            throw new SQLException("Machine introuvable");
        }
        
        // Vérifier si la machine est déjà réservée pendant cette période
        if (isAlreadyReserved(reservation.getId_machine_id(), reservation.getDate_debut(), reservation.getDate_fin())) {
            throw new SQLException("La machine est déjà réservée pendant cette période");
        }
        
        // Ajouter la réservation
        String query = "INSERT INTO reservation (id_machine_id, user_id, date_debut, date_fin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getId_machine_id());
            ps.setInt(2, reservation.getUser_id());
            ps.setDate(3, Date.valueOf(reservation.getDate_debut()));
            ps.setDate(4, Date.valueOf(reservation.getDate_fin()));
            
            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }
        }
        
        // Mettre à jour la disponibilité de la machine
        if (isDateRangeActive(reservation.getDate_debut(), reservation.getDate_fin())) {
            machine.setDisponibilite("Non disponible");
            machineService.update(machine);
        }
    }

    @Override
    public void update(Reservation reservation) throws SQLException {
        // Récupérer la réservation actuelle
        Reservation oldReservation = readById(reservation.getId());
        if (oldReservation == null) {
            throw new SQLException("Réservation introuvable");
        }
        
        // Si les dates ont changé, vérifier les conflits
        if (!oldReservation.getDate_debut().equals(reservation.getDate_debut()) ||
            !oldReservation.getDate_fin().equals(reservation.getDate_fin())) {
            if (isAlreadyReserved(reservation.getId_machine_id(), reservation.getDate_debut(), 
                                reservation.getDate_fin(), reservation.getId())) {
                throw new SQLException("La machine est déjà réservée pendant cette période");
            }
        }
        
        // Mettre à jour la réservation
        String query = "UPDATE reservation SET id_machine_id = ?, user_id = ?, date_debut = ?, date_fin = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, reservation.getId_machine_id());
            ps.setInt(2, reservation.getUser_id());
            ps.setDate(3, Date.valueOf(reservation.getDate_debut()));
            ps.setDate(4, Date.valueOf(reservation.getDate_fin()));
            ps.setInt(5, reservation.getId());
            ps.executeUpdate();
        }
        
        // Mettre à jour la disponibilité de la machine si nécessaire
        updateMachineAvailability(reservation.getId_machine_id());
    }

    @Override
    public void delete(Reservation reservation) throws SQLException {
        String query = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, reservation.getId());
            ps.executeUpdate();
        }
        
        // Mettre à jour la disponibilité de la machine
        updateMachineAvailability(reservation.getId_machine_id());
    }

    @Override
    public List<Reservation> readAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }
        }
        return reservations;
    }

    public Reservation readById(int id) throws SQLException {
        String query = "SELECT * FROM reservation WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractReservationFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public List<Reservation> getByMachineId(int machineId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE id_machine_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, machineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(extractReservationFromResultSet(rs));
                }
            }
        }
        return reservations;
    }
    
    public List<Reservation> getByUserId(int userId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(extractReservationFromResultSet(rs));
                }
            }
        }
        return reservations;
    }
    
    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("id"),
            rs.getInt("id_machine_id"),
            rs.getInt("user_id"),
            rs.getDate("date_debut").toLocalDate(),
            rs.getDate("date_fin").toLocalDate()
        );
    }
    
    private void updateMachineAvailability(int machineId) throws SQLException {
        Machine machine = machineService.readById(machineId);
        if (machine != null) {
            // Vérifier s'il y a des réservations actives pour cette machine
            if (hasActiveReservations(machineId)) {
                machine.setDisponibilite("Non disponible");
            } else {
                machine.setDisponibilite("Disponible");
            }
            machineService.update(machine);
        }
    }
    
    private boolean isDateRangeActive(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        return !endDate.isBefore(today);
    }
    
    private boolean isAlreadyReserved(int machineId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return isAlreadyReserved(machineId, startDate, endDate, 0);
    }
    
    private boolean isAlreadyReserved(int machineId, LocalDate startDate, LocalDate endDate, int excludeReservationId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation " +
                      "WHERE id_machine_id = ? AND id != ? AND " +
                      "((date_debut <= ? AND date_fin >= ?) OR " +
                      "(date_debut <= ? AND date_fin >= ?) OR " +
                      "(date_debut >= ? AND date_fin <= ?))";
        
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, machineId);
            ps.setInt(2, excludeReservationId);
            ps.setDate(3, Date.valueOf(startDate));
            ps.setDate(4, Date.valueOf(startDate));
            ps.setDate(5, Date.valueOf(endDate));
            ps.setDate(6, Date.valueOf(endDate));
            ps.setDate(7, Date.valueOf(startDate));
            ps.setDate(8, Date.valueOf(endDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    private boolean hasActiveReservations(int machineId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation " +
                      "WHERE id_machine_id = ? AND date_fin >= ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, machineId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
