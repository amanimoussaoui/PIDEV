package Agriwise.tools;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BridgeManager {
    private static final int MAX_RETRIES = 8;
    private static final int RETRY_DELAY_MS = 500;

    private WebEngine webEngine;
    private JSObject window;
    private Object bridgeObject;
    private boolean isBridgeReady = false;

    // Constructor
    public BridgeManager(WebEngine webEngine, Object bridgeObject) {
        this.webEngine = webEngine;
        this.bridgeObject = bridgeObject;
        setupBridge();
    }

    private void setupBridge() {
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("Web page loaded, initializing bridge...");
                initializeBridgeWithRetry(0);
            }
        });
    }

    private void initializeBridgeWithRetry(int attempt) {
        Platform.runLater(() -> {
            try {
                window = (JSObject) webEngine.executeScript("window");
                window.setMember("javafx", bridgeObject);

                // Try to ping the connection
                webEngine.executeScript(
                        "if (typeof window.javafx !== 'undefined') {" +
                                "   window.bridgeReady = true;" +
                                "   console.log('Bridge verification successful');" +
                                "   try { window.javafx.ping(); } catch(e) { console.error('Ping error: ' + e); }" +
                                "} else {" +
                                "   console.error('Bridge object not found in window');" +
                                "}"
                );

                isBridgeReady = true;
                System.out.println("Bridge initialized successfully on attempt " + (attempt + 1));
            } catch (Exception e) {
                System.err.println("Bridge initialization attempt " + (attempt + 1) + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    PauseTransition retry = new PauseTransition(Duration.millis(RETRY_DELAY_MS));
                    retry.setOnFinished(ev -> initializeBridgeWithRetry(attempt + 1));
                    retry.play();
                } else {
                    System.err.println("Failed to initialize bridge after " + MAX_RETRIES + " attempts");
                }
            }
        });
    }

    public Object getBridgeConnector() {
        return bridgeObject;
    }

    public void executeWhenReady(Runnable action) {
        if (isBridgeReady) {
            Platform.runLater(action);
        } else {
            CompletableFuture.runAsync(() -> {
                int attempts = 0;
                while (!isBridgeReady && attempts < MAX_RETRIES * 2) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                        attempts++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (isBridgeReady) {
                    Platform.runLater(action);
                } else {
                    System.err.println("Timeout waiting for bridge to initialize");
                }
            });
        }
    }

    public <T> T executeScriptWithResult(String script) {
        if (webEngine != null) {
            try {
                return (T) webEngine.executeScript(script);
            } catch (Exception e) {
                System.err.println("Error executing script: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    // Add a method to execute JavaScript
    public void executeScript(String script) {
        if (webEngine != null) {
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript(script);
                } catch (Exception e) {
                    System.err.println("Error executing script: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    // A safer method that returns whether the script executed successfully
    public CompletableFuture<Boolean> executeScriptSafe(String script) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (webEngine == null) {
            future.complete(false);
            return future;
        }

        Platform.runLater(() -> {
            try {
                webEngine.executeScript(script);
                future.complete(true);
            } catch (Exception e) {
                System.err.println("Error executing script: " + e.getMessage());
                e.printStackTrace();
                future.complete(false);
            }
        });

        return future;
    }
}