package tn.esprit.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.FontWeight;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.models.UserSession;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javax.imageio.ImageIO;

public class ClientMesMaterielleController {

    @FXML
    private TextField searchField;
    
    @FXML
    private FlowPane materielsContainer;
    
    @FXML
    private Button generateQrCodeBtn;
    
    private MachineService machineService;
    private List<Machine> allMachines;
    private Machine selectedMachine;
    
    @FXML
    public void initialize() {
        machineService = new MachineService();
        
        // Vérifier si l'utilisateur est connecté
        UserSession session = UserSession.getInstance();
        if (session == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour voir vos matériels");
            return;
        }
        
        System.out.println("User ID dans ClientMesMaterielleController: " + session.getUserId());
        loadMaterials();
        
        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            materielsContainer.getChildren().clear();
            String searchText = newValue.toLowerCase();
            
            allMachines.stream()
                .filter(machine -> 
                    machine.getNom().toLowerCase().contains(searchText) ||
                    machine.getDescription().toLowerCase().contains(searchText) ||
                    machine.getEtat().toLowerCase().contains(searchText)
                )
                .forEach(this::createMaterialCard);
        });
        
        // Désactiver le bouton générer QR code tant qu'aucun matériel n'est sélectionné
        generateQrCodeBtn.setDisable(true);
    }
    
    private void loadMaterials() {
        try {
            UserSession session = UserSession.getInstance();
            if (session != null) {
                allMachines = machineService.getMachinesByUserId(session.getUserId());
                System.out.println("Nombre de machines trouvées pour l'utilisateur " + session.getUserId() + ": " + allMachines.size());
                materielsContainer.getChildren().clear();
                allMachines.forEach(this::createMaterialCard);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des matériels: " + e.getMessage());
        }
    }
    
    private void createMaterialCard(Machine machine) {
        // Création de la carte
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 5; -fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        
        // Titre (Nom)
        Label titleLabel = new Label(machine.getNom());
        titleLabel.setFont(javafx.scene.text.Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #2e8b57;");
        
        // Prix
        Label prixLabel = new Label(String.format("%.2f DT", machine.getPrix()));
        prixLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px;");
        
        // Description
        Label descLabel = new Label(machine.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #333333;");
        
        // État et Disponibilité
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label etatLabel = new Label("État: " + machine.getEtat());
        etatLabel.setStyle("-fx-text-fill: #666666;");
        
        Label dispoLabel = new Label("• " + machine.getDisponibilite());
        dispoLabel.setStyle("-fx-text-fill: " + 
            (machine.getDisponibilite().equals("Disponible") ? "#2e8b57" : "#ff4444") + ";");
        
        statusBox.getChildren().addAll(etatLabel, dispoLabel);
        
        // Date de maintenance
        Label dateLabel = new Label("Maintenance: " + 
            machine.getDateMaintenance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-text-fill: #666666;");
        
        // Bouton de sélection pour QR code
        Button selectForQrBtn = new Button("Sélectionner pour QR");
        selectForQrBtn.setStyle("-fx-background-color: #4286f4; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
        selectForQrBtn.setOnAction(e -> {
            selectedMachine = machine;
            generateQrCodeBtn.setDisable(false);
            
            // Mettre à jour le style de toutes les cartes pour montrer laquelle est sélectionnée
            materielsContainer.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    if (vbox.getUserData() == machine) {
                        vbox.setStyle("-fx-background-color: #f0f7ff; -fx-border-color: #4286f4; " +
                                     "-fx-border-radius: 5; -fx-background-radius: 5; " +
                                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
                    } else {
                        vbox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                                     "-fx-border-radius: 5; -fx-background-radius: 5; " +
                                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
                    }
                }
            });
        });
        
        // Boutons d'action
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-cursor: hand;");
        modifierBtn.setOnAction(e -> handleModifierMateriel(e));
        modifierBtn.setUserData(machine);
        
        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-cursor: hand;");
        supprimerBtn.setOnAction(e -> handleSupprimerMateriel(machine));
        
        buttonsBox.getChildren().addAll(selectForQrBtn, modifierBtn, supprimerBtn);
        
        // Ajout de tous les éléments à la carte
        card.getChildren().addAll(
            titleLabel,
            prixLabel,
            new Separator(),
            descLabel,
            statusBox,
            dateLabel,
            new Separator(),
            buttonsBox
        );
        
        // Stocker la référence à la machine dans les données utilisateur de la carte
        card.setUserData(machine);
        
        materielsContainer.getChildren().add(card);
    }
    
    @FXML
    private void supprimerMateriel(ActionEvent event) {
        // This method is kept for FXML binding but delegates to the handler
    }

    private void handleSupprimerMateriel(Machine machine) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer ce matériel ?");
        confirmation.setContentText("Cette action ne peut pas être annulée.");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                machineService.delete(machine);
                loadMaterials(); // Recharger la liste
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le matériel a été supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleModifierMateriel(ActionEvent event) {
        try {
            Button button = (Button) event.getSource();
            Machine machine = (Machine) button.getUserData();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_modifier_materiel.fxml"));
            Parent root = loader.load();
            
            ClientModifierMaterielController controller = loader.getController();
            controller.setMachine(machine);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Close the current window
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de la fenêtre de modification");
            alert.setContentText("Une erreur est survenue lors de la tentative d'ouverture de la fenêtre de modification: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void generateQrCodePdf() {
        if (selectedMachine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un matériel pour générer son QR code");
            return;
        }
        
        try {
            // Générer QR code
            BufferedImage qrImage = generateQRCodeImage(selectedMachine);
            
            // Créer un fichier temporaire pour le PDF
            String fileName = selectedMachine.getNom().replaceAll("[^a-zA-Z0-9.-]", "_") + "_QRCode.pdf";
            File tempFile = File.createTempFile("agriwise_qrcode_", ".pdf");
            tempFile.deleteOnExit(); // Le fichier sera supprimé à la fermeture de l'application
            
            // Créer le PDF dans le fichier temporaire
            createPDF(tempFile, qrImage, selectedMachine);
            
            // Ouvrir automatiquement le PDF
            try {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(tempFile);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Le QR code a été généré et ouvert avec succès!\n" +
                        "Le fichier est temporaire et sera supprimé à la fermeture de l'application.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Information", 
                        "Votre système ne permet pas l'ouverture automatique du fichier.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de l'ouverture automatique du fichier: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private BufferedImage generateQRCodeImage(Machine machine) throws WriterException {
        // Créer le contenu du QR code
        String qrContent = String.format(
            "MATERIEL AGRICOLE AGRIWISE\n\n" +
            "Nom: %s\n" +
            "Prix: %.2f DT/jour\n" +
            "État: %s\n" +
            "Disponibilité: %s\n" +
            "Description: %s\n\n" +
            "Date de maintenance: %s\n\n" +
            "Scanned from Agriwise App",
            machine.getNom(),
            machine.getPrix(),
            machine.getEtat(),
            machine.getDisponibilite(),
            machine.getDescription() != null ? machine.getDescription() : "Aucune description",
            machine.getDateMaintenance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        
        // Générer le QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    private void createPDF(File file, BufferedImage qrImage, Machine machine) throws IOException, DocumentException {
        // Créer un nouveau document PDF
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        // Ajouter un titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("QR Code - " + machine.getNom(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Ajouter le QR code
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        Image pdfQrImage = Image.getInstance(baos.toByteArray());
        pdfQrImage.scaleToFit(350, 350); // QR code plus grand
        pdfQrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(pdfQrImage);
        
        // Ajouter une légende pour le QR code
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph qrLegend = new Paragraph("QR Code pour " + machine.getNom(), normalFont);
        qrLegend.setAlignment(Element.ALIGN_CENTER);
        qrLegend.setSpacingBefore(10);
        document.add(qrLegend);
        
        // Ajouter un pied de page
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Généré par l'application Agriwise - " + java.time.LocalDate.now(), 
                                       FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        // Fermer le document
        document.close();
    }
    
    @FXML
    public void goBack(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) materielsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Client - Agriwise");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la page d'accueil: " + e.getMessage());
        }
    }
    
    public void refreshMaterielle() {
        loadMaterials();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 