package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.esprit.models.Formation;
import tn.esprit.services.FormationService;
import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ListFormationsController {

    @FXML
    private TableView<Formation> formationTable;
    @FXML
    private TableColumn<Formation, Integer> idColumn;
    @FXML
    private TableColumn<Formation, String> titreColumn;
    @FXML
    private TableColumn<Formation, String> descriptionColumn;
    @FXML
    private TableColumn<Formation, Float> prixColumn;
    @FXML
    private TableColumn<Formation, java.time.LocalDate> dateColumn;
    @FXML
    private TableColumn<Formation, String> imageColumn;
    @FXML
    private TableColumn<Formation, Void> actionColumn;

    private final FormationService service = new FormationService();

    @FXML
    public void initialize() {
        System.out.println("### Début d'initialisation du contrôleur ListFormations ###");

        try {
            // 1. Initialisation des colonnes
            System.out.println("Initialisation des colonnes...");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

            // 2. Configuration de la colonne image
            System.out.println("Configuration de la colonne image...");
            imageColumn.setCellFactory(column -> new TableCell<>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitHeight(60);
                    imageView.setFitWidth(60);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String imagePath, boolean empty) {
                    super.updateItem(imagePath, empty);
                    if (empty || imagePath == null || imagePath.isEmpty()) {
                        setGraphic(null);
                    } else {
                        try {
                            File file = new File(imagePath);
                            if (file.exists()) {
                                imageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                                setGraphic(imageView);
                            } else {
                                setGraphic(new Label("Image introuvable"));
                                System.out.println("Image non trouvée : " + imagePath);
                            }
                        } catch (Exception e) {
                            setGraphic(new Label("Erreur image"));
                            System.err.println("Erreur de chargement d'image : " + e.getMessage());
                        }
                    }
                }
            });

            // 3. Chargement des données
            System.out.println("Chargement des formations...");
            loadFormations();

            // 4. Ajout des boutons d'action
            System.out.println("Configuration des boutons d'action...");
            addButtonsToTable();

            System.out.println("### Initialisation terminée avec succès ###");

        } catch (Exception e) {
            System.err.println("### ERREUR CRITIQUE DANS L'INITIALISATION ###");
            System.err.println("Type d'erreur : " + e.getClass().getName());
            System.err.println("Message : " + e.getMessage());
            System.err.println("StackTrace :");
            e.printStackTrace();

            // Afficher une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'initialisation");
            alert.setHeaderText("Impossible de charger les formations");
            alert.setContentText("Une erreur technique est survenue : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadFormations() {
        ObservableList<Formation> list = FXCollections.observableArrayList(service.getAll());
        formationTable.setItems(list);
    }

    private void addButtonsToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");
            private final Button updateButton = new Button("Modifier");
            private final HBox buttons = new HBox(5, updateButton, deleteButton);

            {
                // Style des boutons
                updateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");

                // Gestion du clic sur Supprimer
                deleteButton.setOnAction(event -> {
                    Formation formation = getTableView().getItems().get(getIndex());
                    System.out.println("Tentative de suppression de la formation ID: " + formation.getId());

                    // Confirmation avant suppression
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Confirmation");
                    confirmation.setHeaderText("Supprimer la formation");
                    confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette formation ? Les participants recevront une notification.");

                    Optional<ButtonType> result = confirmation.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            service.deleteWithNotification(formation);
                            System.out.println("Suppression et notification terminées");

                            // Afficher un message de succès
                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Succès");
                            success.setContentText("La formation a été supprimée et les participants ont été notifiés.");
                            success.showAndWait();

                            // Rafraîchir la table
                            loadFormations();
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la suppression: " + e.getMessage());
                            e.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setContentText("Échec de la suppression: " + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                });

                // Gestion du clic sur Modifier (inchangé)
                updateButton.setOnAction(event -> {
                    Formation selected = getTableView().getItems().get(getIndex());
                    showUpdateForm(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void showUpdateForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFormation.fxml"));
            Parent root = loader.load();

            FormationController controller = loader.getController();
            controller.prefillForm(formation);  // Remplit les champs

            // Remplacer la vue actuelle
            formationTable.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToAddFormation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddFormation.fxml"));
            formationTable.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToFrontView() {
        try {
            System.out.println("Attempting to load FXML from: " +
                    getClass().getResource("/ListFormationsFront.fxml"));

            Parent root = FXMLLoader.load(getClass().getResource("/ListFormationsFront.fxml"));
            formationTable.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Error loading FXML:");
            e.printStackTrace();

            // Show paths for debugging
            showResourceDebugInfo();
        }
    }

    private void showResourceDebugInfo() {
        System.out.println("=== Resource Debug ===");
        System.out.println("Current directory: " + System.getProperty("user.dir"));
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        // Test different paths
        String[] pathsToTest = {
                "/ListFormationsFront.fxml",
                "ListFormationsFront.fxml",
                "/images/placeholder.png"
        };

        for (String path : pathsToTest) {
            URL url = getClass().getResource(path);
            System.out.println(path + " exists: " + (url != null));
        }
    }
    @FXML
    private void goToParticipationAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListParticipations.fxml"));
            formationTable.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Optionnel : afficher une alerte utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible de charger l'interface des participations.");
            alert.showAndWait();
        }
    }

}
