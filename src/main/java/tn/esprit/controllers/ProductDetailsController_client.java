package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Product;
import tn.esprit.models.UserSession;
import tn.esprit.util.MaConnexion;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class ProductDetailsController_client {

    @FXML
    private Label productNameLabel;

    @FXML
    private ImageView productImage;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label descriptionValueLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label priceValueLabel;

    @FXML
    private Label stockLabel;

    @FXML
    private Label stockValueLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label categoryValueLabel;

    @FXML
    private Label updatedAtLabel;

    @FXML
    private Label updatedAtValueLabel;

    @FXML
    private Label agriwiseTitleLabel;

    @FXML
    private Label agriwiseTextLabel;

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<String> languageSelector;

    @FXML
    private ComboBox<String> currencySelector;

    @FXML
    private VBox reviewsList;

    @FXML
    private ComboBox<String> ratingSelector;

    @FXML
    private TextField commentField;

    @FXML
    private Button submitReviewButton;

    @FXML
    private Label averageRatingLabel;

    private Product product;
    private Stage stage;
    private Connection connect;

    private Map<String, String> originalStaticTexts;
    private Map<String, String> originalDynamicTexts;
    private String currentLanguage = "fr";
    private String currentCurrency = "TND";
    private Map<String, Double> exchangeRates;

    @FXML
    public void initialize() {
        originalStaticTexts = new HashMap<>();
        originalStaticTexts.put("descriptionLabel", "Description:");
        originalStaticTexts.put("priceLabel", "Prix:");
        originalStaticTexts.put("stockLabel", "Stock:");
        originalStaticTexts.put("categoryLabel", "Catégorie:");
        originalStaticTexts.put("updatedAtLabel", "Mis à jour:");
        originalStaticTexts.put("agriwiseTitleLabel", "🔎 Pourquoi choisir les produits AgriWise ?");
        originalStaticTexts.put("agriwiseTextLabel", "✅ AgriWise propose des produits agricoles innovants, durables et de haute qualité, adaptés aux besoins des professionnels, pour une agriculture efficace, écoresponsable et accompagnée par des experts");

        try {
            loadExchangeRates();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Impossible de charger les taux de change. Les prix seront affichés en TND par défaut.");
        }

        if (currencySelector != null) {
            currencySelector.setOnAction(event -> updateCurrency());
        }

        connect = MaConnexion.getInstance().getCon();
    }

    public void setData(Product product, Stage stage) {
        this.product = product;
        this.stage = stage;

        originalDynamicTexts = new HashMap<>();
        originalDynamicTexts.put("productName", product.getNom());
        originalDynamicTexts.put("description", product.getDescription());
        originalDynamicTexts.put("price", String.valueOf(product.getPrix()));
        originalDynamicTexts.put("stock", String.valueOf(product.getStock()));
        originalDynamicTexts.put("category", product.getCategory());
        originalDynamicTexts.put("updatedAt", product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : "N/A");

        productNameLabel.setText(product.getNom());
        descriptionValueLabel.setText(product.getDescription());
        priceValueLabel.setText(product.getPrix() + " TND");
        stockValueLabel.setText(String.valueOf(product.getStock()));
        categoryValueLabel.setText(product.getCategory());
        updatedAtValueLabel.setText(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : "N/A");

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imagePath = "file:" + product.getImage();
            Image image = new Image(imagePath, 250, 250, false, true);
            productImage.setImage(image);
        }
    }

    @FXML
    private void returnToMarket() {
        try {
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void translateInterface() {
        try {
            String selectedLanguage = languageSelector.getValue();
            String targetLang = selectedLanguage.substring(selectedLanguage.length() - 3, selectedLanguage.length() - 1);

            if (targetLang.equals(currentLanguage)) {
                return;
            }

            currentLanguage = targetLang;

            // Si la langue cible est le français, restaurer directement les textes originaux
            if (targetLang.equals("fr")) {
                restoreOriginalTexts();
                return;
            }

            // Traduire les textes statiques
            for (Map.Entry<String, String> entry : originalStaticTexts.entrySet()) {
                String translatedText = translateText(entry.getValue(), "fr", targetLang);
                switch (entry.getKey()) {
                    case "descriptionLabel":
                        descriptionLabel.setText(translatedText);
                        break;
                    case "priceLabel":
                        priceLabel.setText(translatedText);
                        break;
                    case "stockLabel":
                        stockLabel.setText(translatedText);
                        break;
                    case "categoryLabel":
                        categoryLabel.setText(translatedText);
                        break;
                    case "updatedAtLabel":
                        updatedAtLabel.setText(translatedText);
                        break;
                    case "agriwiseTitleLabel":
                        agriwiseTitleLabel.setText(translatedText);
                        break;
                    case "agriwiseTextLabel":
                        agriwiseTextLabel.setText(translatedText);
                        break;
                }
            }

            // Traduire les textes dynamiques
            productNameLabel.setText(translateText(originalDynamicTexts.get("productName"), "fr", targetLang));
            descriptionValueLabel.setText(translateText(originalDynamicTexts.get("description"), "fr", targetLang));
            stockValueLabel.setText(translateText(originalDynamicTexts.get("stock"), "fr", targetLang));
            categoryValueLabel.setText(translateText(originalDynamicTexts.get("category"), "fr", targetLang));
            updatedAtValueLabel.setText(translateText(originalDynamicTexts.get("updatedAt"), "fr", targetLang));

            updateCurrency();
        } catch (Exception e) {
            e.printStackTrace();
            restoreOriginalTexts();
            showErrorAlert("Impossible de traduire les textes. Vérifiez votre connexion Internet ou essayez une autre instance de l'API de traduction.");
        }
    }

    private void loadExchangeRates() throws Exception {
        exchangeRates = new HashMap<>();
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/tnd.json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur lors de la récupération des taux de change: " + response.statusCode());
        }

        JSONObject responseJson = new JSONObject(response.body());
        JSONObject rates = responseJson.getJSONObject("tnd");

        exchangeRates.put("TND", 1.0);
        exchangeRates.put("USD", rates.getDouble("usd"));
        exchangeRates.put("EUR", rates.getDouble("eur"));
        exchangeRates.put("GBP", rates.getDouble("gbp"));
    }

    @FXML
    private void updateCurrency() {
        if (exchangeRates == null || exchangeRates.isEmpty()) {
            showErrorAlert("Taux de change non disponibles. Les prix sont affichés en TND par défaut.");
            return;
        }

        String selectedCurrency = currencySelector.getValue();
        String currencyCode = selectedCurrency.substring(0, 3);

        if (currencyCode.equals(currentCurrency)) {
            return;
        }

        currentCurrency = currencyCode;

        try {
            double originalPrice = Double.parseDouble(originalDynamicTexts.get("price"));
            double rate = exchangeRates.getOrDefault(currencyCode, 1.0);
            double convertedPrice = originalPrice * rate;
            String formattedPrice = String.format("%.2f", convertedPrice);
            priceValueLabel.setText(formattedPrice + " " + currencyCode);

            String translatedPriceLabel = translateText(originalStaticTexts.get("priceLabel"), "fr", currentLanguage);
            // Si nous sommes en français, pas besoin de traduire à nouveau
            if (currentLanguage.equals("fr")) {
                priceLabel.setText(originalStaticTexts.get("priceLabel"));
            } else {
                priceLabel.setText(translatedPriceLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la conversion des devises. Les prix sont affichés en TND par défaut.");
            priceValueLabel.setText(originalDynamicTexts.get("price") + " TND");
        }
    }

    private void restoreOriginalTexts() {
        descriptionLabel.setText(originalStaticTexts.get("descriptionLabel"));
        priceLabel.setText(originalStaticTexts.get("priceLabel"));
        stockLabel.setText(originalStaticTexts.get("stockLabel"));
        categoryLabel.setText(originalStaticTexts.get("categoryLabel"));
        updatedAtLabel.setText(originalStaticTexts.get("updatedAtLabel"));
        agriwiseTitleLabel.setText(originalStaticTexts.get("agriwiseTitleLabel"));
        agriwiseTextLabel.setText(originalStaticTexts.get("agriwiseTextLabel"));

        productNameLabel.setText(originalDynamicTexts.get("productName"));
        descriptionValueLabel.setText(originalDynamicTexts.get("description"));
        priceValueLabel.setText(originalDynamicTexts.get("price") + " TND");
        stockValueLabel.setText(originalDynamicTexts.get("stock"));
        categoryValueLabel.setText(originalDynamicTexts.get("category"));
        updatedAtValueLabel.setText(originalDynamicTexts.get("updatedAt"));
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String translateText(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.isEmpty()) {
            return text;
        }

        HttpClient client = HttpClient.newHttpClient();
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        String langPair = URLEncoder.encode(sourceLang + "|" + targetLang, StandardCharsets.UTF_8.toString());
        String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + langPair;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("Erreur lors de la traduction: " + response.body());
            throw new RuntimeException("Erreur lors de la traduction: " + response.statusCode());
        }

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getJSONObject("responseData").getString("translatedText");
    }
}