package Agriwise.services;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PredictionService {
    private final CultureService cultureService;

    public PredictionService(CultureService cultureService) {
        this.cultureService = cultureService;
    }
    private static final String API_URL = "http://localhost:5000/predict";

    /**
     * Predicts the yield for a culture
     * @param culture The culture to predict yield for
     * @return Predicted yield in kg
     * @throws Exception If there's an error during prediction
     */
    public double predictYield(Culture culture) throws Exception {
        // Validate input
        if (culture == null || culture.getParcelle() == null) {
            throw new IllegalArgumentException("Culture and Parcelle cannot be null");
        }

        // Create JSON payload
        JSONObject payload = createPredictionPayload(culture);

        // Call Python API
        return callPythonApi(payload);
    }

    /**
     * Creates the prediction payload based on the culture data
     */
    private JSONObject createPredictionPayload(Culture culture) throws Exception {
        // Get valid crop names from database
        List<String> validCrops = cultureService.getAllCultureNames();

        // Validate input crop
        String nomCulture = culture.getNomCulture();
        if (!validCrops.contains(nomCulture)) {
            throw new IllegalArgumentException("Culture non supportée: " + nomCulture +
                    ". Cultures valides: " + validCrops);
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
        inputData.put("culture_id", 0);

        // Add all crop type fields dynamically
        for (String crop : validCrops) {
            String columnName = "crop_type_" + crop.replace(" ", "_");
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
        inputData.put("soil_type_argileux", parcelle.getTypeSol().equals("argileux") ? 1 : 0);
        inputData.put("soil_type_sableux", parcelle.getTypeSol().equals("sableux") ? 1 : 0);
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
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getDouble("predicted_yield");
            }
        } else {
            throw new Exception("HTTP error code: " + responseCode);
        }
    }
}