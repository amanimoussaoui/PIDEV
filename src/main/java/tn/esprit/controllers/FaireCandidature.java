package tn.esprit.controllers;

import javafx.scene.control.*;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tn.esprit.models.Candidature;
import tn.esprit.services.ServiceCandidature;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import tn.esprit.models.Terrain;
import tn.esprit.models.UserSession;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;
import tn.esprit.models.Utilisateur;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public class FaireCandidature {

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextArea butArea;
    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, LocalDate> colDateDebut;
    @FXML private TableColumn<Candidature, LocalDate> colDateFin;
    @FXML private TableColumn<Candidature, String> colBut;
    @FXML private TableColumn<Candidature, Double> colPrixTerrain;
    @FXML private TableColumn<Candidature, String> colEtat;
    @FXML private TextField montantField; // Ajoutez cette ligne si ce n'est pas déjà fait
    @FXML private TextField etatField;
    @FXML
    private TextArea recommandationArea;
    private String accessToken;;
    private int idTerrain;

    private int getConnectedUserId() {
        UserSession session = UserSession.getInstance();
        if (session == null) {
            throw new IllegalStateException("Aucun utilisateur connecté");
        }
        return session.getUserId();
    }

    public void setIdTerrain(int id) {
        this.idTerrain = id;
    }

    public void setMontant(double montant) {
        montantField.setText(String.valueOf(montant));
    }
    private Terrain terrainSelectionne;  // Ajoutez cette variable pour stocker le terrain

    private Candidature candidatureSelectionnee;
    @FXML

    public void initialize() {
        // Récupérer l'ID de l'utilisateur connecté
        int userId = UserSession.getInstance().getUserId();

        // Charger seulement les candidatures de cet utilisateur
        List<Candidature> liste = new ServiceCandidature().getCandidaturesByUtilisateurId(userId);
        ObservableList<Candidature> data = FXCollections.observableArrayList(liste);
        tableCandidatures.setItems(data);

// Configuration des colonnes
        colDateDebut.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateDebut()));
        colDateFin.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateFin()));
        colBut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBut()));
        colEtat.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEtat()));

// Configuration de la colonne prix
        colPrixTerrain.setCellValueFactory(cellData -> {
            Double prix = cellData.getValue().getMontant();
            return new SimpleObjectProperty<>(prix != null ? prix : 0.0);
        });

        // Désactiver l'édition des champs
        montantField.setEditable(false);
        etatField.setEditable(false);
        etatField.setText("en attente");

    }

    private void loadCandidatures() {
        int userId = UserSession.getInstance().getUserId();

        ServiceCandidature service = new ServiceCandidature();
        // Récupérer les candidatures faites uniquement par l'utilisateur connecté
        List<Candidature> candidatures = service.getCandidaturesByUtilisateurId(userId);

        ServiceTerrain serviceTerrain = new ServiceTerrain();
        for (Candidature candidature : candidatures) {
            // Associer le terrain correspondant à chaque candidature
            Terrain terrain = serviceTerrain.getById(candidature.getIdTerrainId());
            candidature.setTerrain(terrain);
        }

        // Convertir la liste en ObservableList pour le TableView
        ObservableList<Candidature> observableList = FXCollections.observableArrayList(candidatures);
        tableCandidatures.setItems(observableList);
    }


    @FXML
    private void validerCandidature() {
        try {
            // Validation des entrées
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();
            String but = butArea.getText().trim();

            // Contrôles de saisie
            if (dateDebut == null || dateFin == null || but.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs.");
                return;
            }

            if (dateDebut.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "La date de début doit être aujourd'hui ou ultérieure.");
                return;
            }

            if (!dateFin.isAfter(dateDebut)) {
                showAlert(Alert.AlertType.ERROR, "La date de fin doit être après la date de début.");
                return;
            }

            if (but.length() < 10) {
                showAlert(Alert.AlertType.ERROR, "Le but doit contenir au moins 10 caractères.");
                return;
            }

            if (terrainSelectionne == null) {
                showAlert(Alert.AlertType.ERROR, "Aucun terrain sélectionné.");
                return;
            }

            // Récupération de l'utilisateur connecté
            UserSession session = UserSession.getInstance();
            if (session == null) {
                showAlert(Alert.AlertType.ERROR, "Aucun utilisateur connecté.");
                return;
            }

            int connectedUserId = session.getUserId();
            Utilisateur connectedUser = session.getUtilisateurConnecte();

            ServiceCandidature service = new ServiceCandidature();

            // Mode ajout
            if (candidatureSelectionnee == null) {
                // Vérification d'unicité
                if (service.existeCandidature(connectedUserId, terrainSelectionne.getId(), dateDebut, dateFin, but)) {
                    showAlert(Alert.AlertType.ERROR, "Une candidature similaire existe déjà.");
                    return;
                }

                // Création de la nouvelle candidature
                Candidature nouvelleCandidature = new Candidature();
                nouvelleCandidature.setDateDebut(dateDebut);
                nouvelleCandidature.setDateFin(dateFin);
                nouvelleCandidature.setBut(but);
                nouvelleCandidature.setEtat("en attente");
                nouvelleCandidature.setMontant(terrainSelectionne.getPrix());
                nouvelleCandidature.setIdTerrainId(terrainSelectionne.getId());

                // Association de l'utilisateur connecté
                nouvelleCandidature.setUtilisateurId(connectedUserId);
                nouvelleCandidature.setUtilisateur(connectedUser);

                // Ajout à la base de données
                service.ajouter(nouvelleCandidature);

                showAlert(Alert.AlertType.INFORMATION, "Candidature envoyée avec succès !");
            }
            // Mode modification
            else {
                // Vérification des modifications
                if (!dateDebut.equals(candidatureSelectionnee.getDateDebut()) ||
                        !dateFin.equals(candidatureSelectionnee.getDateFin()) ||
                        !but.equals(candidatureSelectionnee.getBut())) {

                    // Vérification d'unicité seulement si modification
                    if (service.existeCandidature(connectedUserId, terrainSelectionne.getId(), dateDebut, dateFin, but)) {
                        showAlert(Alert.AlertType.ERROR, "Une candidature similaire existe déjà.");
                        return;
                    }
                }

                // Mise à jour
                candidatureSelectionnee.setDateDebut(dateDebut);
                candidatureSelectionnee.setDateFin(dateFin);
                candidatureSelectionnee.setBut(but);

                if (service.modifier(candidatureSelectionnee)) {
                    showAlert(Alert.AlertType.INFORMATION, "Candidature modifiée avec succès !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Échec de la modification de la candidature.");
                }
            }

            resetForm();
            loadCandidatures();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void resetForm() {
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        butArea.clear();
        candidatureSelectionnee = null;
    }
    public void setTerrainSelectionne(Terrain terrain) {
        this.terrainSelectionne = terrain;
        if (terrain != null) {
            montantField.setText(String.valueOf(terrain.getPrix()));
            this.idTerrain = terrain.getId(); // Mettre à jour l'ID du terrain aussi
        }
    }

    @FXML
    private void modifierCandidature() {
        // Sélectionner la candidature dans le TableView
        candidatureSelectionnee = tableCandidatures.getSelectionModel().getSelectedItem();

        // Vérifier si une candidature a bien été sélectionnée
        if (candidatureSelectionnee == null) {
            // Si aucune candidature n'est sélectionnée, afficher un message d'alerte
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une candidature à modifier");
            return;
        }

        // Remplir les champs du formulaire avec les données de la candidature sélectionnée
        dateDebutPicker.setValue(candidatureSelectionnee.getDateDebut());
        dateFinPicker.setValue(candidatureSelectionnee.getDateFin());
        butArea.setText(candidatureSelectionnee.getBut());

        // Une fois les champs remplis, on effectue la modification dans la base de données
        try {
            // Récupération des nouvelles valeurs du formulaire
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();
            String but = butArea.getText().trim();

            // Contrôle de saisie : vérifier si les valeurs sont valides
            if (dateDebut == null || dateFin == null || but.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Tous les champs doivent être remplis");
                return;
            }

            if (dateDebut.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "La date de début ne peut pas être avant aujourd'hui");
                return;
            }

            if (!dateFin.isAfter(dateDebut)) {
                showAlert(Alert.AlertType.ERROR, "La date de fin doit être après la date de début");
                return;
            }

            if (but.length() < 10) {
                showAlert(Alert.AlertType.ERROR, "Le but doit contenir au moins 10 caractères");
                return;
            }

            // Mettre à jour les champs de la candidature sélectionnée
            candidatureSelectionnee.setDateDebut(dateDebut);
            candidatureSelectionnee.setDateFin(dateFin);
            candidatureSelectionnee.setBut(but);

            // Appeler le service pour modifier la candidature dans la base de données
            ServiceCandidature service = new ServiceCandidature();
            boolean isUpdated = service.modifier(candidatureSelectionnee);  // On passe la candidature complète

            if (isUpdated) {
                showAlert(Alert.AlertType.INFORMATION, "Candidature modifiée avec succès !");
                loadCandidatures();  // Recharger la liste des candidatures pour afficher les modifications
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de la modification de la candidature");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void supprimerCandidature() {
        candidatureSelectionnee = tableCandidatures.getSelectionModel().getSelectedItem();

        if (candidatureSelectionnee == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une candidature à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette candidature ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ServiceCandidature service = new ServiceCandidature();
            boolean suppressionReussie = service.supprimer(candidatureSelectionnee.getId());

            if (suppressionReussie) {
                showAlert(Alert.AlertType.INFORMATION, "Candidature supprimée avec succès");
                loadCandidatures();
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de la suppression");
            }
        }
    }
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void genererRecommandation() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une candidature d'abord !");
            return;
        }

        String but = selected.getBut();
        String prompt = "Donne des conseils pratiques pour améliorer cette candidature agricole :\n" + but;

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "openai/gpt-4o"); // modèle utilisé
                requestBody.put("max_tokens", 500);

                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);
                messages.put(message);

                requestBody.put("messages", messages);

                Request request = new Request.Builder()
                        .url("https://openrouter.ai/api/v1/chat/completions")
                        .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json; charset=utf-8")))
                        .addHeader("Authorization", "Bearer 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")
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur API HTTP: " + response.code()));
                        return;
                    }

                    String responseBody = response.body().string();
                    System.out.println("Réponse API brute: " + responseBody);

                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("error")) {
                        String errorMessage = jsonResponse.getJSONObject("error").getString("message");
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur API : " + errorMessage));
                    } else if (jsonResponse.has("choices")) {
                        String recommendation = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        Platform.runLater(() -> recommandationArea.setText(recommendation));
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Réponse inattendue de l'API."));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()));
            }
        }).start();
    }



    private Properties loadConfig() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new IOException("Fichier config.properties non trouvé dans les ressources");
            }
            prop.load(input);
        }
        return prop;
    }

    // Ajoutez cette méthode dans votre classe
    private String getApiKey() {
        try {
            return loadConfig().getProperty("deepseek.api.key");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de configuration API");
            throw new RuntimeException("API key not configured properly");
        }
    }




}
