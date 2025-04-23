package Agriwise.tools;

import Agriwise.controllers.Activite.ActiviteController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.util.Duration;
import netscape.javascript.JSObject;

public class BridgeManager {
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 300;

    private WebEngine webEngine;
    private JSObject window;
    private ActiviteController.JavaBridge bridge;
    private boolean isBridgeReady = false;

    public BridgeManager(WebEngine webEngine, ActiviteController controller) {
        this.webEngine = webEngine;
        this.bridge = new ActiviteController.JavaBridge(controller);
        setupBridge();
    }


    private void setupBridge() {
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                initializeBridgeWithRetry(0);
            }
        });
    }

    private void initializeBridgeWithRetry(int attempt) {
        Platform.runLater(() -> {
            try {
                window = (JSObject) webEngine.executeScript("window");
                window.setMember("javafx", bridge);

                // Verify bridge is ready
                webEngine.executeScript(
                        "if (typeof window.javafx !== 'undefined') {" +
                                "   window.bridgeReady = true;" +
                                "   console.log('Bridge verification successful');" +
                                "}"
                );

                isBridgeReady = true;
                System.out.println("Bridge successfully initialized on attempt " + (attempt + 1));
            } catch (Exception e) {
                if (attempt < MAX_RETRIES - 1) {
                    System.out.println("Bridge initialization failed, retrying... (" + (attempt + 1) + ")");
                    PauseTransition retry = new PauseTransition(Duration.millis(RETRY_DELAY_MS));
                    retry.setOnFinished(ev -> initializeBridgeWithRetry(attempt + 1));
                    retry.play();
                } else {
                    System.err.println("Failed to initialize bridge after " + MAX_RETRIES + " attempts");
                }
            }
        });
    }

    public void executeWhenReady(Runnable action) {
        if (isBridgeReady) {
            action.run();
        } else {
            new Thread(() -> {
                int attempts = 0;
                while (!isBridgeReady && attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                        attempts++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Platform.runLater(action);
            }).start();
        }
    }
}