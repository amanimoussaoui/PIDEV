package Agriwise.services;

import Agriwise.entities.Parcelle;
import Agriwise.interfaces.IParcelleService;
import Agriwise.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParcelleService implements IParcelleService {
    private Connection cnx;

    public ParcelleService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public void addParcelle(Parcelle p) throws SQLException {
        // First check if parcelle already exists
        if (isParcelleExists(p)) {
            throw new SQLException("Une parcelle avec ces informations existe déjà");
        }

        String req = "INSERT INTO parcelle (nom, superficie, localisation, type_sol, latitude, longitude, boundary, map_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());
            ps.setFloat(5, p.getLatitude());
            ps.setFloat(6, p.getLongitude());
            ps.setString(7, p.getBoundaryJson());
            ps.setString(8, p.getMapImage());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e; // Re-throw the exception to handle it in the controller
        }
    }

    @Override
    public void updateParcelle(Parcelle p) {
        String req = "UPDATE parcelle SET nom=?, superficie=?, localisation=?, type_sol=?, latitude=?, longitude=?, boundary=?, map_image=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());
            ps.setFloat(5, p.getLatitude());
            ps.setFloat(6, p.getLongitude());
            ps.setString(7, p.getBoundaryJson());
            ps.setString(8, p.getMapImage());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteParcelle(int id) {
        String req = "DELETE FROM parcelle WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Parcelle getParcelleById(int id) {
        String req = "SELECT * FROM parcelle WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return resultSetToParcelle(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Parcelle> getAllParcelles() {
        List<Parcelle> list = new ArrayList<>();
        String req = "SELECT * FROM parcelle";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(resultSetToParcelle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Parcelle resultSetToParcelle(ResultSet rs) throws SQLException {
        Parcelle p = new Parcelle();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setSuperficie(rs.getFloat("superficie"));
        p.setLocalisation(rs.getString("localisation"));
        p.setTypeSol(rs.getString("type_sol"));
        p.setLatitude(rs.getFloat("latitude"));
        p.setLongitude(rs.getFloat("longitude"));

        String boundaryJson = rs.getString("boundary");
        if (boundaryJson != null && !boundaryJson.isEmpty()) {
            p.setBoundaryFromJson(boundaryJson);
        }

        p.setMapImage(rs.getString("map_image"));
        return p;
    }

    public boolean isParcelleExists(Parcelle p) {
        String req = "SELECT COUNT(*) FROM parcelle WHERE nom = ? AND superficie = ? AND localisation = ? AND type_sol = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}