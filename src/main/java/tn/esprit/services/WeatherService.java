package tn.esprit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import tn.esprit.models.WeatherInfo;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class WeatherService {
    // Nouvelle clé API OpenWeatherMap

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherInfo getWeatherInfo(double latitude, double longitude) throws IOException {
        System.out.println("=== Début de la récupération des données météo ===");
        System.out.println("Coordonnées : lat=" + latitude + ", lon=" + longitude);
        
        String url = String.format("%sweather?lat=%f&lon=%f&appid=%s&units=metric&lang=fr",
                BASE_URL, latitude, longitude, API_KEY);
        System.out.println("URL de l'API : " + url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            System.out.println("Envoi de la requête HTTP...");
            
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Code de réponse HTTP : " + statusCode);
                
                String json = EntityUtils.toString(response.getEntity());
                System.out.println("Réponse reçue. Contenu JSON : " + json);

                if (statusCode == 401) {
                    throw new IOException("Erreur d'authentification avec l'API OpenWeatherMap. Veuillez vérifier votre clé API.");
                } else if (statusCode != 200) {
                    throw new IOException("Erreur API OpenWeatherMap. Code : " + statusCode + ", Réponse : " + json);
                }
                
                WeatherInfo info = parseWeatherResponse(json);
                System.out.println("Données météo analysées avec succès : " + info.toString());
                return info;
            }
        } catch (Exception e) {
            System.err.println("!!! ERREUR lors de la récupération des données météo !!!");
            System.err.println("Type d'erreur : " + e.getClass().getName());
            System.err.println("Message d'erreur : " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de la récupération des données météo: " + e.getMessage(), e);
        }
    }

    public String checkAdverseWeatherConditions(double latitude, double longitude, 
                                              LocalDate startDate, LocalDate endDate) throws IOException {
        System.out.println("=== Début de la vérification des conditions météorologiques ===");
        System.out.println("Coordonnées : lat=" + latitude + ", lon=" + longitude);
        System.out.println("Période : du " + startDate + " au " + endDate);
        

        String url = String.format("%sforecast?lat=%f&lon=%f&appid=%s&units=metric&lang=fr",
                BASE_URL, latitude, longitude, API_KEY);
        System.out.println("URL de l'API : " + url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            System.out.println("Envoi de la requête HTTP...");
            
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Code de réponse HTTP : " + statusCode);
                
                String json = EntityUtils.toString(response.getEntity());
                System.out.println("Réponse reçue. Longueur JSON : " + json.length());
                
                if (statusCode == 401) {
                    System.err.println("Erreur d'authentification avec l'API OpenWeatherMap");
                    throw new IOException("Erreur d'authentification avec l'API OpenWeatherMap. Veuillez vérifier votre clé API.");
                } else if (statusCode != 200) {
                    System.err.println("Erreur API OpenWeatherMap. Code : " + statusCode);
                    throw new IOException("Erreur API OpenWeatherMap. Code : " + statusCode + ", Réponse : " + json);
                }
                
                String conditions = parseAdverseConditions(json, startDate, endDate);
                if (conditions != null) {
                    System.out.println("Conditions défavorables détectées : " + conditions);
                } else {
                    System.out.println("Aucune condition défavorable détectée");
                }
                return conditions;
            }
        } catch (Exception e) {
            System.err.println("!!! ERREUR lors de la vérification des conditions météorologiques !!!");
            System.err.println("Type d'erreur : " + e.getClass().getName());
            System.err.println("Message d'erreur : " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de la vérification des conditions météorologiques: " + e.getMessage(), e);
        }
    }

    private String parseAdverseConditions(String json, LocalDate startDate, LocalDate endDate) throws IOException {
        System.out.println("Analyse des conditions météorologiques...");
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.has("list")) {
                throw new IOException("Le nœud 'list' est manquant dans la réponse JSON");
            }
            
            JsonNode list = root.path("list");
            StringBuilder adverseConditions = new StringBuilder();
            int conditionsCount = 0;

            for (JsonNode forecast : list) {
                // Convertir le timestamp en LocalDateTime
                long timestamp = forecast.path("dt").asLong();
                LocalDateTime forecastDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    ZoneId.systemDefault()
                );
                LocalDate date = forecastDate.toLocalDate();

                // Vérifier si la date est dans la plage demandée
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    System.out.println("Analyse des prévisions pour le " + date);
                    
                    JsonNode weather = forecast.path("weather").get(0);
                    String description = weather.path("description").asText();
                    double temperature = forecast.path("main").path("temp").asDouble();
                    int humidity = forecast.path("main").path("humidity").asInt();
                    double windSpeed = forecast.path("wind").path("speed").asDouble();

                    System.out.println("Conditions : " + description + ", Temp: " + temperature + 
                                     "°C, Humidité: " + humidity + "%, Vent: " + windSpeed + " m/s");

                    // Vérifier les conditions défavorables
                    if (isAdverseCondition(description, temperature, humidity, windSpeed)) {
                        conditionsCount++;
                        if (adverseConditions.length() > 0) {
                            adverseConditions.append("\n");
                        }
                        adverseConditions.append(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                       .append(" à ").append(forecastDate.format(DateTimeFormatter.ofPattern("HH:mm")))
                                       .append(": ").append(description)
                                       .append(" (").append(String.format("%.1f°C", temperature)).append(")");
                        
                        System.out.println("Condition défavorable détectée !");
                    }
                }
            }

            System.out.println("Nombre total de conditions défavorables trouvées : " + conditionsCount);
            return adverseConditions.length() > 0 ? adverseConditions.toString() : null;
            
        } catch (Exception e) {
            System.err.println("!!! ERREUR lors de l'analyse des conditions météorologiques !!!");
            System.err.println("JSON reçu : " + json);
            System.err.println("Message d'erreur : " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de l'analyse des conditions météorologiques: " + e.getMessage(), e);
        }
    }

    private boolean isAdverseCondition(String description, double temperature, int humidity, double windSpeed) {
        String desc = description.toLowerCase();
        
        // Conditions météorologiques défavorables
        boolean badWeather = desc.contains("pluie") || desc.contains("orage") || 
                           desc.contains("neige") || desc.contains("grêle") ||
                           desc.contains("tempête") || desc.contains("brouillard");
        
        // Conditions extrêmes
        boolean extremeTemp = temperature < 0 || temperature > 35;
        boolean highHumidity = humidity > 85;
        boolean strongWind = windSpeed > 10.8; // > 39 km/h

        boolean isAdverse = badWeather || extremeTemp || (highHumidity && strongWind);
        
        if (isAdverse) {
            System.out.println("Condition défavorable détectée:");
            System.out.println("- Description: " + description);
            System.out.println("- Température: " + temperature + "°C");
            System.out.println("- Humidité: " + humidity + "%");
            System.out.println("- Vitesse du vent: " + windSpeed + " m/s");
        }
        
        return isAdverse;
    }

    private WeatherInfo parseWeatherResponse(String json) throws IOException {
        try {
            System.out.println("Début de l'analyse de la réponse JSON...");
            JsonNode root = objectMapper.readTree(json);
            
            WeatherInfo weatherInfo = new WeatherInfo();
            
            // Vérification des nœuds principaux
            if (!root.has("main")) {
                throw new IOException("Le nœud 'main' est manquant dans la réponse JSON");
            }
            if (!root.has("weather") || !root.path("weather").isArray() || root.path("weather").size() == 0) {
                throw new IOException("Le nœud 'weather' est manquant ou invalide dans la réponse JSON");
            }
            
            // Température
            JsonNode mainNode = root.path("main");
            weatherInfo.setTemperature(mainNode.path("temp").asDouble());
            System.out.println("Température récupérée : " + weatherInfo.getTemperature());
            
            // Description météo
            JsonNode weatherNode = root.path("weather").get(0);
            String description = weatherNode.path("description").asText();
            weatherInfo.setDescription(description);
            System.out.println("Description récupérée : " + description);
            
            // Humidité
            weatherInfo.setHumidity(mainNode.path("humidity").asInt());
            
            // Vitesse du vent
            if (root.has("wind")) {
                weatherInfo.setWindSpeed(root.path("wind").path("speed").asDouble());
            }
            
            // Pression atmosphérique
            weatherInfo.setPressure(mainNode.path("pressure").asInt());
            
            // Couverture nuageuse
            if (root.has("clouds")) {
                weatherInfo.setCloudCover(root.path("clouds").path("all").asInt());
            }
            
            // Lever et coucher du soleil
            if (root.has("sys")) {
                JsonNode sysNode = root.path("sys");
                if (sysNode.has("sunrise") && sysNode.has("sunset")) {
                    long sunriseTimestamp = sysNode.path("sunrise").asLong();
                    long sunsetTimestamp = sysNode.path("sunset").asLong();
                    
                    weatherInfo.setSunrise(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(sunriseTimestamp),
                        ZoneId.systemDefault()
                    ));
                    
                    weatherInfo.setSunset(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(sunsetTimestamp),
                        ZoneId.systemDefault()
                    ));
                }
            }
            
            // Icône météo
            String iconCode = weatherNode.path("icon").asText();
            weatherInfo.setIconUrl(String.format("http://openweathermap.org/img/w/%s.png", iconCode));
            System.out.println("URL de l'icône : " + weatherInfo.getIconUrl());
            
            // Nom de la ville et pays
            weatherInfo.setCity(root.path("name").asText());
            if (root.has("sys")) {
                weatherInfo.setCountry(root.path("sys").path("country").asText());
            }
            
            System.out.println("Analyse JSON terminée avec succès");
            return weatherInfo;
            
        } catch (Exception e) {
            System.err.println("!!! ERREUR lors de l'analyse du JSON !!!");
            System.err.println("JSON reçu : " + json);
            System.err.println("Message d'erreur : " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de l'analyse des données météo: " + e.getMessage(), e);
        }
    }
} 