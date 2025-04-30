package tn.esprit.controllers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import javafx.scene.control.Label;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Desktop;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ProfileService;
import tn.esprit.services.UtilisateurService;
import tn.esprit.models.UserSession; // ✅ Correct import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

public class Dashboard {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableColumn<Utilisateur, Date> tbdinscription;

    @FXML
    private TableColumn<Utilisateur, String> tbemail;

    @FXML
    private TableColumn<Utilisateur, Integer> tbid;

    @FXML
    private TableColumn<Utilisateur, String> tbnom;

    @FXML
    private TableColumn<Utilisateur, String> tbprenom;

    @FXML
    private TableColumn<Utilisateur, String> tbrole;

    @FXML
    private TableView<Utilisateur> tbuserlist;
    @FXML
    private Button btnmainmenu;

    @FXML
    private TextField recherchetextfield;
    @FXML
    private Button btnexcel;
    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label adminUsersLabel;

    /////////////////////////////////////////////////////////////////////////////
    @FXML
    void adduser(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Adminadd.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir la scène Adminadd.");
            alert.showAndWait();
        }
    }


    /////////////////////////////////////////////////////////////////////////////
    private void loadUserData() {
        UtilisateurService us = new UtilisateurService();
        ObservableList<Utilisateur> users = FXCollections.observableArrayList(us.getAllUtilisateurs());
        tbuserlist.setItems(users);
    }

    /////////////////////////////////////////////////////////////////////////////
    @FXML
    void deleteuser(ActionEvent event) {
        Utilisateur selectedUser = tbuserlist.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            ProfileService ps = new ProfileService();
            ps.supprimerProfile(selectedUser.getId_utilisateur());

            UtilisateurService us = new UtilisateurService();
            us.deleteUtilisateur(selectedUser.getId_utilisateur());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Suppression");
            alert.setHeaderText(null);
            alert.setContentText("Utilisateur et profil supprimés avec succès !");
            alert.showAndWait();

            loadUserData();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à supprimer.");
            alert.showAndWait();
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @FXML
    void updateuser(ActionEvent event) {
        Utilisateur selectedUser = tbuserlist.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            try {
                Updateuser.setSelectedUser(selectedUser);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateuser.fxml"));
                Parent root = loader.load();

                Scene scene = ((Node) event.getSource()).getScene();
                scene.setRoot(root);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement de la page Updateuser.fxml");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à modifier.");
            alert.showAndWait();
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @FXML
    void redirecttomenu(ActionEvent event) {
        try {
            // Load the MainmenuScene.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/product_list.fxml"));

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la scène MainmenuScene.fxml");
        }
    }
//////////////////////////////////////////////////////////////////////////////////
@FXML
void rechercheutilisateur(String keyword) {
    if (!keyword.isEmpty()) {
        UtilisateurService us = new UtilisateurService();
        List<Utilisateur> resultats = us.rechercheUtilisateurs(keyword);
        ObservableList<Utilisateur> observableList = FXCollections.observableArrayList(resultats);
        tbuserlist.setItems(observableList);
    } else {
        loadUserData(); // reload full list if search field is empty
    }
}
    //////////////////////////////////////////////////////////////////////////////
    public void rechercheutilisateur(ActionEvent actionEvent) {
    }
    //////////////////////////////////////////////////////////////////////////////

    @FXML
    void importtoexcel(ActionEvent event) {
        UtilisateurService utilisateurService = new UtilisateurService();
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Utilisateurs");

        // Create fonts and styles
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short)12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // En-tête
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nom", "Prénom", "Email", "Rôles", "Date d'inscription"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Contenu
        int rowNum = 1;
        for (Utilisateur u : utilisateurs) {
            Row row = sheet.createRow(rowNum++);

            Cell idCell = row.createCell(0);
            idCell.setCellValue(u.getId_utilisateur());
            idCell.setCellStyle(dataStyle);

            Cell nomCell = row.createCell(1);
            nomCell.setCellValue(u.getNom());
            nomCell.setCellStyle(dataStyle);

            Cell prenomCell = row.createCell(2);
            prenomCell.setCellValue(u.getPrenom());
            prenomCell.setCellStyle(dataStyle);

            Cell emailCell = row.createCell(3);
            emailCell.setCellValue(u.getEmail());
            emailCell.setCellStyle(dataStyle);

            Cell rolesCell = row.createCell(4);
            rolesCell.setCellValue(String.join(", ", u.getRoles()));
            rolesCell.setCellStyle(dataStyle);

            Cell dateCell = row.createCell(5);
            dateCell.setCellValue(u.getDate_inscription().toString());
            dateCell.setCellStyle(dataStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            File file = new File("utilisateurs.xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            System.out.println("Fichier Excel créé : " + file.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Ouverture automatique non supportée.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////////////
    @FXML
    void initialize() {
        assert tbdinscription != null : "fx:id=\"tbdinscription\" was not injected: check your FXML file 'Dashboard.fxml'.";
        assert tbemail != null : "fx:id=\"tbemail\" was not injected: check your FXML file 'Dashboard.fxml'.";
        assert tbid != null : "fx:id=\"tbid\" was not injected: check your FXML file 'Dashboard.fxml'.";
        assert tbnom != null : "fx:id=\"tbnom\" was not injected: check your FXML file 'Dashboard.fxml'.";
        assert tbprenom != null : "fx:id=\"tbprenom\" was not injected: check your FXML file 'Dashboard.fxml'.";
        assert tbrole != null : "fx:id=\"tbrole\" was not injected: check your FXML file 'Dashboard.fxml'.";
        assert tbuserlist != null : "fx:id=\"tbuserlist\" was not injected: check your FXML file 'Dashboard.fxml'.";

        // ✅ Use your session class properly
        int connectedUserId = UserSession.getInstance().getUserId();
        String connectedUsername = UserSession.getInstance().getUserName();
        System.out.println("Connected user ID: " + connectedUserId);
        System.out.println("Connected username: " + connectedUsername);

        UtilisateurService service = new UtilisateurService();
        ObservableList<Utilisateur> list = FXCollections.observableArrayList(service.getAllUtilisateurs());

        tbid.setCellValueFactory(new PropertyValueFactory<>("id_utilisateur"));
        tbnom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tbprenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        tbemail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tbrole.setCellValueFactory(new PropertyValueFactory<>("rolesAsString"));
        tbdinscription.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        int totalUsers = service.getTotalUsers();
        int totalAdmins = service.getTotalAdmins();

        totalUsersLabel.setText("Total Users: " + totalUsers);
        adminUsersLabel.setText("Admins: " + totalAdmins);


        tbuserlist.setItems(list);
        // Add listener to the search text field
        recherchetextfield.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                rechercheutilisateur(newValue);  // Call your search method with the new value
            } else {
                loadUserData(); // Reload all users when the search field is empty
            }
        });
    }


}
