package GestionAgricole.controllers.Activite;

import GestionAgricole.entities.Activite;
import GestionAgricole.services.ActiviteService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class ActiviteController implements Initializable {

    @FXML private WebView webView;
    @FXML private VBox calendarContainer;
    @FXML private Button addButton;

    private ActiviteService activiteService;
    private WebEngine webEngine;
    private JSObject window;

    public class JavaBridge {
        public void log(String message) {
            System.out.println("JavaBridge log: " + message);
        }

        public void callAddActivity(String dateStr) {
            System.out.println("JavaBridge.callAddActivity ENTERED with date: " + dateStr);

            Platform.runLater(() -> {
                System.out.println("Platform.runLater executing");
                try {
                    openActivityForm(dateStr);
                    System.out.println("openActivityForm completed");
                } catch (Exception e) {
                    System.err.println("Error in openActivityForm: ");
                    e.printStackTrace();
                }
            });
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activiteService = new ActiviteService();
        webEngine = webView.getEngine();

        System.out.println("Initializing ActiviteController");

        // Initialize add button action
        addButton.setOnAction(event -> {
            System.out.println("Add button clicked");
            openActivityForm(LocalDate.now().toString());
        });

        // Load the HTML template with the calendar
        loadCalendar();
    }

    private void setupJavaScriptBridge() {
        System.out.println("Setting up JavaScript bridge");

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                try {
                    // Get the window object and inject the JavaBridge
                    window = (JSObject) webEngine.executeScript("window");
                    JavaBridge bridge = new JavaBridge();
                    window.setMember("javafx", bridge);

                    // Verify bridge is working by executing a test function
                    webEngine.executeScript(
                            "if (window.javafx) { " +
                                    "   window.javafx.log('JavaScript bridge test - SUCCESS'); " +
                                    "   console.log('JavaFX bridge is available'); " +
                                    "} else { " +
                                    "   console.error('JavaFX bridge is NOT available'); " +
                                    "}"
                    );

                    // Update calendar with events after page is fully loaded
                    updateCalendarEvents();
                } catch (Exception e) {
                    System.err.println("Error setting up JavaScript bridge: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void openActivityForm(String dateStr) {
        try {
            System.out.println("Opening activity form with date: " + dateStr);

            // Parse the date
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(dateStr);
                System.out.println("Successfully parsed date: " + parsedDate);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dateStr);
                parsedDate = LocalDate.now();
            }

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAgricole/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            // Initialize the controller
            ActiviteFormController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller is null after loading FXML");
            }
            System.out.println("Controller initialized successfully");

            // Set the initial date
            controller.setInitialDate(parsedDate);
            System.out.println("Initial date set successfully");

            // Set the refresh callback
            controller.setRefreshCallback(this::refreshCalendar);

            // Create and display the stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            System.out.println("Showing activity form stage");
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error loading activity form: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println("Controller initialization error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in openActivityForm: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadCalendar() {
        System.out.println("Loading calendar");

        // Load the HTML content
        URL htmlUrl = getClass().getResource("/GestionAgricole/views/Activite/calendarTemplate.html");
        if (htmlUrl == null) {
            System.err.println("HTML file not found!");
            return;
        }

        webEngine.load(htmlUrl.toExternalForm());

        // Setup JavaScript bridge after loading the page
        setupJavaScriptBridge();
    }

    private void updateCalendarEvents() {
        System.out.println("Updating calendar events");

        List<Activite> activites = activiteService.getAllActivites();
        StringBuilder eventsJson = new StringBuilder("[");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < activites.size(); i++) {
            Activite activite = activites.get(i);
            String dateStr = activite.getDate() != null ?
                    dateFormat.format(activite.getDate()) : "";
            String type = activite.getType() != null ? activite.getType() : "Autre";
            String description = activite.getDescription() != null ? activite.getDescription() : "";
            int id = activite.getId();

            eventsJson.append("{")
                    .append("\"id\":").append(id).append(",")
                    .append("\"title\":\"").append(escapeJs(type)).append("\",")
                    .append("\"start\":\"").append(escapeJs(dateStr)).append("\",")
                    .append("\"description\":\"").append(escapeJs(description)).append("\",")
                    .append("\"classNames\":[\"").append(escapeJs(type)).append("\"]")
                    .append("}");

            if (i < activites.size() - 1) {
                eventsJson.append(",");
            }
        }
        eventsJson.append("]");

        final String jsonEvents = eventsJson.toString();
        System.out.println("Calendar events JSON prepared");

        Platform.runLater(() -> {
            try {
                if (window != null) {
                    window.call("updateCalendarEvents", jsonEvents);
                    System.out.println("Calendar events updated successfully");
                } else {
                    System.err.println("Window object is null, cannot update calendar events");
                }
            } catch (Exception e) {
                System.err.println("Error updating calendar events: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void refreshCalendar() {
        System.out.println("Refreshing calendar");
        // Only update the events without reloading the entire page
        updateCalendarEvents();
    }
}