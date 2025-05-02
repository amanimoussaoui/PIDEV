package Agriwise.services;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredictionService {
    private final CultureService cultureService;
    private static final String API_URL = "http://localhost:5000/predict";
    private static final Logger logger = Logger.getLogger(PredictionService.class.getName());

    public PredictionService(CultureService cultureService) {
        this.cultureService = cultureService;
    }

    /**
     * Predicts the yield for a culture
     * @param culture The culture to predict yield for
     * @return Predicted yield in kg
     * @throws Exception If there's an error during prediction
     */
    public double predictYield(Culture culture) throws Exception {
        // Log the prediction request
        logger.info("Starting yield prediction for culture: " + culture.getId());

        // Validate input
        if (culture == null || culture.getParcelle() == null) {
            logger.severe("Culture or Parcelle is null");
            throw new IllegalArgumentException("Culture and Parcelle cannot be null");
        }

        // Create JSON payload
        JSONObject payload = createPredictionPayload(culture);
        logger.info("Created prediction payload: " + payload.toString());

        // Call Python API
        try {
            double result = callPythonApi(payload);
            logger.info("Prediction successful: " + result);
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during prediction", e);
            throw new Exception("Failed to get prediction: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the prediction payload based on the culture data
     */
    private JSONObject createPredictionPayload(Culture culture) throws Exception {
        // Get valid crop names from database
        List<String> validCrops = cultureService.getAllCultureNames();
        logger.info("Valid crops: " + validCrops);

        // Validate input crop
        String nomCulture = culture.getNomCulture();
        if (!validCrops.contains(nomCulture)) {
            String errorMsg = "Culture non supportée: " + nomCulture + ". Cultures valides: " + validCrops;
            logger.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        Parcelle parcelle = culture.getParcelle();
        Date dateSemis = culture.getDateSemis();
        int duree = culture.getDuree();
        String statut = culture.getStatut();

        // Calculate harvest date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateSemis);
        calendar.add(Calendar.DAY_OF_MONTH, duree);
        Date harvestDate = calendar.getTime();

        // Format for extracting date components
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

        JSONObject inputData = new JSONObject();
        inputData.put("culture_id", culture.getId() != 0 ? culture.getId() : 0);

        // Add all crop type fields dynamically
        for (String crop : validCrops) {
            String columnName = "crop_type_" + crop;
            inputData.put(columnName, nomCulture.equals(crop) ? 1 : 0);
        }

        // Add sowing date components
        inputData.put("sowing_year", Integer.parseInt(yearFormat.format(dateSemis)));
        inputData.put("sowing_month", Integer.parseInt(monthFormat.format(dateSemis)));
        inputData.put("sowing_day", Integer.parseInt(dayFormat.format(dateSemis)));

        // Add harvest date components
        inputData.put("harvest_year", Integer.parseInt(yearFormat.format(harvestDate)));
        inputData.put("harvest_month", Integer.parseInt(monthFormat.format(harvestDate)));
        inputData.put("harvest_day", Integer.parseInt(dayFormat.format(harvestDate)));

        // Add growth duration
        inputData.put("growth_duration", duree);

        // Add parcelle data
        inputData.put("area", parcelle.getSuperficie());

        // Handle soil type
        String soilType = parcelle.getTypeSol();
        inputData.put("soil_type_argileux", soilType.equals("argileux") ? 1 : 0);
        inputData.put("soil_type_sableux", soilType.equals("sableux") ? 1 : 0);

        inputData.put("latitude", parcelle.getLatitude());
        inputData.put("longitude", parcelle.getLongitude());

        // Add yield quality placeholders
        inputData.put("yield_quality_Bonne", 0);
        inputData.put("yield_quality_Excellente", 0);
        inputData.put("yield_quality_Moyenne", 1);

        // Add status one-hot encoded fields
        inputData.put("status_en_culture", statut.equals("en_culture") ? 1 : 0);
        inputData.put("status_terminé", statut.equals("terminé") ? 1 : 0);

        return inputData;
    }

    /**
     * Calls the Python API with the prepared payload
     */
    private double callPythonApi(JSONObject payload) throws Exception {
        HttpURLConnection connection = null;
        try {
            // Create connection
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);  // 5 seconds timeout for connection
            connection.setReadTimeout(10000);    // 10 seconds timeout for read
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send the request
            String requestPayload = payload.toString();
            logger.info("Sending request to " + API_URL + ": " + requestPayload);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            logger.info("Response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    String responseStr = response.toString();
                    logger.info("Response from API: " + responseStr);

                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(responseStr);
                    return jsonResponse.getDouble("predicted_yield");
                }
            } else {
                // Read the error response
                String errorResponse = readErrorStream(connection);
                logger.severe("API error response: " + errorResponse);
                throw new Exception("HTTP error code: " + responseCode + ", Response: " + errorResponse);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error during API call", e);
            throw new Exception("IO error during API call: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Reads the error stream from the connection
     */
    private String readErrorStream(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (Exception e) {
            return "Could not read error stream: " + e.getMessage();
        }
    }
}