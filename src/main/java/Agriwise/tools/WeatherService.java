package Agriwise.tools;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherService {
    private static final String API_KEY = "63c0003e890ffa6c0fb123f100583b4f"; // Replace with your API key
    private final Map<String, CachedWeather> cache = new HashMap<>();
    private final Map<String, CachedForecast> forecastCache = new HashMap<>();

    /**
     * Get current weather data for a specific location
     */
    public JSONObject getWeatherData(float latitude, float longitude) {
        String cacheKey = latitude + "_" + longitude;

        // Check cache first
        if (cache.containsKey(cacheKey)) {
            CachedWeather cachedData = cache.get(cacheKey);
            // If cache is still valid (30 min)
            if (System.currentTimeMillis() - cachedData.timestamp < 1800000) {
                return cachedData.data;
            }
        }

        try {
            String urlStr = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric",
                    latitude, longitude, API_KEY
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONParser parser = new JSONParser();
            JSONObject weatherData = (JSONObject) parser.parse(response.toString());

            // Cache the result
            cache.put(cacheKey, new CachedWeather(weatherData));

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("error", "Unable to fetch weather data: " + e.getMessage());
            return error;
        }
    }

    /**
     * Get 5-day forecast data for a specific location
     */
    public List<JSONObject> getForecastData(float latitude, float longitude) {
        String cacheKey = latitude + "_" + longitude;

        // Check cache first
        if (forecastCache.containsKey(cacheKey)) {
            CachedForecast cachedData = forecastCache.get(cacheKey);
            // If cache is still valid (3 hours)
            if (System.currentTimeMillis() - cachedData.timestamp < 10800000) {
                return cachedData.data;
            }
        }

        try {
            String urlStr = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric&cnt=40",
                    latitude, longitude, API_KEY
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONParser parser = new JSONParser();
            JSONObject forecastResponse = (JSONObject) parser.parse(response.toString());

            // Extract daily forecasts (one per day)
            List<JSONObject> dailyForecasts = processForecastData(forecastResponse);

            // Cache the result
            forecastCache.put(cacheKey, new CachedForecast(dailyForecasts));

            return dailyForecasts;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Process the forecast data to extract one forecast per day
     */
    private List<JSONObject> processForecastData(JSONObject forecastResponse) {
        List<JSONObject> dailyForecasts = new ArrayList<>();
        Map<String, JSONObject> dayMap = new HashMap<>();

        try {
            JSONArray list = (JSONArray) forecastResponse.get("list");

            // Group forecasts by day
            for (Object item : list) {
                JSONObject forecast = (JSONObject) item;
                String dt = forecast.get("dt").toString();
                String dateStr = dt.substring(0, 10); // Get just the date part

                // Keep only one forecast per day (noon forecast preferable)
                if (!dayMap.containsKey(dateStr) ||
                        dt.contains("12:00:00")) {
                    dayMap.put(dateStr, forecast);
                }
            }

            // Take only first 5 days
            int count = 0;
            for (JSONObject forecast : dayMap.values()) {
                dailyForecasts.add(forecast);
                count++;
                if (count >= 5) break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dailyForecasts;
    }

    private static class CachedWeather {
        final JSONObject data;
        final long timestamp;

        CachedWeather(JSONObject data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class CachedForecast {
        final List<JSONObject> data;
        final long timestamp;

        CachedForecast(List<JSONObject> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
}