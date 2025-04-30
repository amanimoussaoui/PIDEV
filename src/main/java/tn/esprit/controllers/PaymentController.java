package tn.esprit.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.models.Panier;

import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Handler;

public class PaymentController {

    private static final Logger LOGGER = Logger.getLogger(PaymentController.class.getName());

    static {
        try {
            System.setProperty("file.encoding", "UTF-8");
            for (Handler handler : LOGGER.getParent().getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    ((ConsoleHandler) handler).setEncoding("UTF-8");
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la configuration de l'encodage UTF-8 pour les journaux : " + e.getMessage());
        }
    }

    @FXML
    private Label totalAmountLabel;

    @FXML
    private ListView<String> cartItemsList;

    @FXML
    private WebView stripeWebView;

    @FXML
    private Label loadingLabel;

    @FXML
    private Button backButton;

    private List<Panier> cartItems;
    private double totalAmount;
    private String checkoutSessionUrl;
    private String clientName; // Nouvelle propriété pour stocker le nom du client

    // Clés Stripe et Twilio
    private final String STRIPE_SECRET_KEY = "sk_test_51Qx7xJQs57apLOtFHzAXY0EIGlO7x1IlzMYTJ8NHbGjeBGw6Q1lvI2zmjXtuJySqDevw5Bya2HTGORp4LYq182R000SWPksKUd";
    private final String TWILIO_ACCOUNT_SID = "ACca1d9ffd6822b682b96ee07b01e261f8";
    private final String TWILIO_AUTH_TOKEN = "990b9fce73dc09cc007b70f4e1ffa0b0";
    private final String TWILIO_PHONE_NUMBER = "+15707474573";
    private final String TO_PHONE_NUMBER = "+21695921917";
    private final String SUCCESS_MESSAGE = "Le paiement a été effectué avec succès. Merci pour votre achat !";

    public void initialize() {
        // Initialiser Stripe
        Stripe.apiKey = STRIPE_SECRET_KEY;
        // Initialiser Twilio
        Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
        // Configurer le WebView
        Platform.runLater(() -> {
            if (stripeWebView != null) {
                stripeWebView.getEngine().setJavaScriptEnabled(true);
            }
        });
    }

    public void setCartItems(List<Panier> cartItems, String clientName) {
        if (cartItems == null || cartItems.isEmpty()) {
            LOGGER.warning("La liste cartItems est vide ou nulle");
            Platform.runLater(() -> showAlert("Erreur", "Le panier est vide."));
            return;
        }
        this.cartItems = cartItems;
        this.clientName = clientName; // Définir le nom du client
        LOGGER.info("Nombre d'éléments dans le panier : " + cartItems.size());
        calculateTotal();
        Platform.runLater(() -> {
            totalAmountLabel.setText(String.format("Total: %.2f USD", totalAmount));
            cartItemsList.getItems().clear();
            for (Panier panier : cartItems) {
                cartItemsList.getItems().add(String.format("%s - Quantité: %d - Prix: %.2f USD",
                        panier.getProduct().getNom(), panier.getQuantite(), panier.getTotale()));
            }
        });
        createStripeCheckoutSession();
    }

    private void calculateTotal() {
        totalAmount = 0.0;
        for (Panier panier : cartItems) {
            LOGGER.info("Produit: " + panier.getProduct().getNom() + ", Quantité: " + panier.getQuantite() + ", Total: " + panier.getTotale());
            totalAmount += panier.getTotale();
        }
        LOGGER.info("Montant total calculé : " + totalAmount);
    }

    private void createStripeCheckoutSession() {
        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/success")
                    .setCancelUrl("http://localhost:8080/cancel");

            for (Panier panier : cartItems) {
                builder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount((long) (panier.getProduct().getPrix() * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(panier.getProduct().getNom())
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity((long) panier.getQuantite())
                                .build()
                );
            }

            Session session = Session.create(builder.build());
            checkoutSessionUrl = session.getUrl();
            LOGGER.info("URL de la session de paiement : " + checkoutSessionUrl);
            Platform.runLater(() -> {
                loadingLabel.setVisible(true);
                stripeWebView.getEngine().setJavaScriptEnabled(true);
                stripeWebView.getEngine().load(checkoutSessionUrl);
                stripeWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    LOGGER.info("État du chargement du WebView : " + newState);
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        loadingLabel.setVisible(false);
                    } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                        LOGGER.severe("Échec du chargement du WebView");
                        // Ignorer l'échec si une redirection a déjà été traitée
                        if (!stripeWebView.getEngine().getLocation().contains("/success") &&
                                !stripeWebView.getEngine().getLocation().contains("/cancel")) {
                            loadingLabel.setVisible(false);
                            showAlert("Erreur", "Impossible de charger la page de paiement Stripe.");
                        }
                    }
                });
                stripeWebView.getEngine().locationProperty().addListener((obs, oldLocation, newLocation) -> {
                    LOGGER.info("Redirection détectée : " + newLocation);
                    if (newLocation.contains("/success")) {
                        Platform.runLater(() -> {
                            sendTwilioSMS();
                            showAlert("Succès", "Paiement effectué avec succès !");
                            returnToMarket();
                        });
                    } else if (newLocation.contains("/cancel")) {
                        Platform.runLater(() -> {
                            showAlert("Annulé", "Le paiement a été annulé.");
                            returnToCart();
                        });
                    }
                });
            });
        } catch (StripeException e) {
            LOGGER.severe("Erreur lors de la création de la session Stripe : " + e.getMessage());
            Platform.runLater(() -> {
                loadingLabel.setVisible(false);
                showAlert("Erreur", "Erreur lors de la création de la session de paiement : " + e.getMessage());
            });
        }
    }

    private void sendTwilioSMS() {
        try {
            // Construire le message SMS avec le nom du client et le total du panier
            String smsMessage = String.format(
                    "%s\nClient: %s\nTotal du panier: %.2f USD",
                    SUCCESS_MESSAGE, clientName != null ? clientName : "Inconnu", totalAmount
            );
            Message message = Message.creator(
                    new PhoneNumber(TO_PHONE_NUMBER),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    smsMessage
            ).create();
            LOGGER.info("SMS envoyé à " + TO_PHONE_NUMBER + " avec le message : " + smsMessage);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'envoi du SMS : " + e.getMessage());
            Platform.runLater(() -> showAlert("Erreur", "Erreur lors de l'envoi du SMS : " + e.getMessage()));
        }
    }

    @FXML
    private void returnToCart() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/panier.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            LOGGER.severe("Erreur lors du retour au panier : " + e.getMessage());
            Platform.runLater(() -> showAlert("Erreur", "Erreur lors du retour au panier : " + e.getMessage()));
        }
    }

    private void returnToMarket() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/market.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            LOGGER.severe("Erreur lors du retour au marché : " + e.getMessage());
            Platform.runLater(() -> showAlert("Erreur", "Erreur lors du retour au marché : " + e.getMessage()));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}