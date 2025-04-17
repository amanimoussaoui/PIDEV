package tn.esprit.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Classe utilitaire pour afficher des alertes dans l'application.
 */
public class AlertUtils {
    
    /**
     * Affiche une alerte d'erreur.
     * 
     * @param message Le message d'erreur à afficher
     */
    public static void showError(String message) {
        showAlert(AlertType.ERROR, "Erreur", message);
    }
    
    /**
     * Affiche une alerte d'information.
     * 
     * @param message Le message d'information à afficher
     */
    public static void showInfo(String message) {
        showAlert(AlertType.INFORMATION, "Information", message);
    }
    
    /**
     * Affiche une alerte d'avertissement.
     * 
     * @param title Le titre de l'alerte
     * @param message Le message d'avertissement à afficher
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }
    
    /**
     * Affiche une alerte.
     * 
     * @param alertType Le type d'alerte
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 