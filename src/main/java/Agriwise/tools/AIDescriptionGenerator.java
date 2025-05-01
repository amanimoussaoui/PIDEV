package Agriwise.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileInputStream;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * A utility class to generate descriptions for agricultural activities
 * using Google's Gemini AI model through API calls.
 */
public class AIDescriptionGenerator {

    private static final String CONFIG_FILE = "src/main/java/Agriwise/tools/config.properties";
    private static final String DEFAULT_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String DEFAULT_MODEL = "gemini-1.5-flash";

    private String apiKey;
    private String apiUrl;
    private String model;

    /**
     * Creates a new AIDescriptionGenerator.
     * Attempts to load API configuration from properties file.
     */
    public AIDescriptionGenerator() {
        // Load configuration
        try {
            Properties config = new Properties();
            FileInputStream input = new FileInputStream(CONFIG_FILE);
            config.load(input);

            this.apiKey = config.getProperty("ai.api.key");
            this.apiUrl = config.getProperty("ai.api.url", DEFAULT_API_URL);
            this.model = config.getProperty("ai.api.model", DEFAULT_MODEL);

            System.out.println("Using API URL: " + this.apiUrl);
            System.out.println("API Key configured: " + (this.apiKey != null && !this.apiKey.isEmpty()));

            input.close();
        } catch (IOException e) {
            System.err.println("Failed to load AI configuration: " + e.getMessage());

            // Use demo mode when no API key is available
            this.apiKey = null;
            this.apiUrl = DEFAULT_API_URL;
            this.model = DEFAULT_MODEL;
        }
    }

    /**
     * Generates a description based on the activity type and culture name using Gemini AI.
     *
     * @param activityType The type of activity
     * @param cultureName The name of the culture
     * @param additionalInfo Additional context information (like date)
     * @return A generated description
     * @throws IOException If there's an issue with the API call
     */
    public String generateDescription(String activityType, String cultureName, String additionalInfo) throws IOException {
        // If API key is not available, use demo mode
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            return generateDemoDescription(activityType, cultureName, additionalInfo);
        }

        try {
            // Create the full URL with API key for Gemini
            String fullUrl = apiUrl + "?key=" + apiKey;

            // Create connection
            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create prompt
            String prompt = createPrompt(activityType, cultureName, additionalInfo);

            // Create request payload for Gemini API
            String payload = "{"
                    + "\"contents\": ["
                    + "  {"
                    + "    \"role\": \"user\","
                    + "    \"parts\": ["
                    + "      {"
                    + "        \"text\": \"" + prompt + "\""
                    + "      }"
                    + "    ]"
                    + "  }"
                    + "],"
                    + "\"generationConfig\": {"
                    + "  \"temperature\": 0.7,"
                    + "  \"maxOutputTokens\": 100,"
                    + "  \"topP\": 0.8,"
                    + "  \"topK\": 40"
                    + "}"
                    + "}";

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Parse JSON response from Gemini
                JSONObject jsonResponse = new JSONObject(response.toString());

                // Print the raw JSON for debugging
                System.out.println("Raw API response: " + jsonResponse.toString());

                // Fixed parsing logic to match Gemini API structure
                String content = "";
                try {
                    content = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")  // Changed from getJSONArray to getJSONObject
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                } catch (Exception e) {
                    System.err.println("Error parsing JSON response: " + e.getMessage());
                    System.err.println("Response structure: " + jsonResponse.toString());

                    // Try alternate parsing approaches if the structure is different
                    try {
                        // Alternative approach 1
                        content = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                    } catch (Exception e2) {
                        try {
                            // Alternative approach 2 - just get the first text field we can find
                            if (jsonResponse.has("candidates") && jsonResponse.getJSONArray("candidates").length() > 0) {
                                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);

                                // Try to navigate through the structure
                                if (candidate.has("content")) {
                                    JSONObject contentObj = candidate.getJSONObject("content");
                                    if (contentObj.has("parts") && contentObj.getJSONArray("parts").length() > 0) {
                                        JSONObject part = contentObj.getJSONArray("parts").getJSONObject(0);
                                        if (part.has("text")) {
                                            content = part.getString("text");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e3) {
                            System.err.println("All parsing attempts failed. Using fallback method.");
                            // If all parsing attempts fail, fall back to demo mode
                            return generateDemoDescription(activityType, cultureName, additionalInfo);
                        }
                    }
                }

                // Clean up the result (remove quotes if present)
                return content.replaceAll("^\"|\"$", "").trim();
            } else {
                // Handle error responses
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                System.err.println("API request failed with status " + responseCode +
                        ": " + errorResponse.toString());
                throw new IOException("API request failed with status " + responseCode +
                        ": " + errorResponse.toString());
            }
        } catch (Exception e) {
            // Fall back to demo mode if API call fails
            System.err.println("API call failed: " + e.getMessage());
            return generateDemoDescription(activityType, cultureName, additionalInfo);
        }
    }

    /**
     * Creates a prompt for the AI model based on the input parameters.
     */
    private String createPrompt(String activityType, String cultureName, String additionalInfo) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Agis comme un expert agricole. Génère une description concise (max 15 mots) pour une activité agricole de type '")
                .append(activityType)
                .append("' pour la culture '")
                .append(cultureName)
                .append("'.");

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            promptBuilder.append(" Cette activité a lieu le ").append(additionalInfo).append(".");
        }

        promptBuilder.append(" Sois précis et direct, sans phrases d'introduction ou conclusion.");

        return promptBuilder.toString();
    }

    /**
     * Generates a description without using an API call (demo mode).
     * Used as a fallback when API key is not available or API call fails.
     */
    private String generateDemoDescription(String activityType, String cultureName, String additionalInfo) {
        // Fallback templates based on activity type
        String[] arrosageTemplates = {
                "Arrosage des %s pour maintenir l'humidité du sol",
                "Irrigation des %s selon les besoins hydriques",
                "Arrosage ciblé des %s en fonction de la météo"
        };

        String[] elagageTemplates = {
                "Élagage des %s pour une meilleure production",
                "Taille des branches des %s pour améliorer l'aération",
                "Élagage des %s pour favoriser la fructification"
        };

        String[] traitementTemplates = {
                "Traitement préventif des %s contre les maladies fongiques",
                "Application de produits phytosanitaires sur les %s",
                "Protection des %s contre les ravageurs de saison"
        };

        String[] fertilisationTemplates = {
                "Fertilisation organique des %s pour enrichir le sol",
                "Apport d'engrais adapté aux besoins des %s",
                "Amendement du sol pour la nutrition des %s"
        };

        String[] recolteTemplates = {
                "Récolte manuelle des %s à maturité optimale",
                "Cueillette sélective des %s prêts à être consommés",
                "Récolte des %s selon les critères de qualité"
        };

        String[] defaultTemplates = {
                "%s des %s selon les bonnes pratiques agricoles",
                "%s optimal des %s pour maximiser le rendement",
                "%s des %s selon le calendrier cultural"
        };

        // Select templates based on activity type
        String[] templates;
        switch (activityType) {
            case "Arrosage":
                templates = arrosageTemplates;
                break;
            case "Élagage":
                templates = elagageTemplates;
                break;
            case "Traitement":
                templates = traitementTemplates;
                break;
            case "Fertilisation":
                templates = fertilisationTemplates;
                break;
            case "Récolte":
                templates = recolteTemplates;
                break;
            default:
                templates = defaultTemplates;
                break;
        }

        // Select a random template
        int randomIndex = ThreadLocalRandom.current().nextInt(templates.length);
        String template = templates[randomIndex];

        // Format the template with the culture name and activity type
        return String.format(template, activityType.equals("Arrosage") ||
                activityType.equals("Élagage") ||
                activityType.equals("Traitement") ||
                activityType.equals("Fertilisation") ||
                activityType.equals("Récolte") ? cultureName : activityType, cultureName);
    }
}