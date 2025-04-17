package tn.esprit.services;

import tn.esprit.interfaces.IServiceF;
import tn.esprit.models.Formation;
import tn.esprit.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService implements IServiceF<Formation> {
    Connection cnx = MaConnexion.getInstance().getCon();

    @Override
    public void add(Formation formation) {
        String req = "INSERT INTO formation(titre, description, prix, date, image) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, formation.getTitre());
            pst.setString(2, formation.getDescription());
            pst.setFloat(3, formation.getPrix());
            pst.setDate(4, java.sql.Date.valueOf(formation.getDate()));
            pst.setString(5, formation.getImage());
            pst.executeUpdate();
            System.out.println("Formation ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Formation formation) {
        String req = "UPDATE formation SET titre = ?, description = ?, prix = ?, date = ?, image = ? WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, formation.getTitre());
            pst.setString(2, formation.getDescription());
            pst.setFloat(3, formation.getPrix());
            pst.setDate(4, java.sql.Date.valueOf(formation.getDate()));
            pst.setString(5, formation.getImage());
            pst.setInt(6, formation.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Formation mise à jour avec succès !");
            } else {
                System.out.println("Aucune formation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Formation formation) {
        String req = "DELETE FROM formation WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, formation.getId());

            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Formation supprimée avec succès !");
            } else {
                System.out.println("Aucune formation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Formation> getAll() {
        List<Formation> formations = new ArrayList<>();
        String req = "SELECT * FROM formation";
        try {
            Statement st = cnx.createStatement();
            ResultSet res = st.executeQuery(req);
            while (res.next()) {
                Formation formation = new Formation();
                formation.setId(res.getInt("id"));
                formation.setTitre(res.getString("titre"));
                formation.setDescription(res.getString("description"));
                formation.setPrix(res.getFloat("prix"));
                java.sql.Date sqlDate = res.getDate("date");
                if (sqlDate != null) {
                    formation.setDate(sqlDate.toLocalDate());
                }
                formation.setImage(res.getString("image"));
                formations.add(formation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return formations;
    }

    @Override
    public Formation getOne(int id) {
        return null;
    }
}
