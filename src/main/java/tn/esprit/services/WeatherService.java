package tn.esprit.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for providing weather information (simulated for offline use)
 */
public class WeatherService {
    
    // Utilisation de données météo simulées au lieu d'appeler une API externe
    private final Random random = new Random();
    
    // Conditions météo possibles en français
    private final String[] weatherConditions = {
        "Ciel dégagé", "Nuageux", "Partiellement nuageux", 
        "Pluie légère", "Pluie modérée", "Orage", 
        "Brouillard", "Neige légère", "Ensoleillé"
    };
    
    // Conditions défavorables
    private final String[] adverseConditions = {
        "Pluie modérée", "Pluie forte", "Orage", 
        "Grêle", "Tempête", "Neige abondante"
    };
    
    /**
     * Gets the current weather information for a specific location
     * @param latitude location latitude
     * @param longitude location longitude
     * @return WeatherInfo object containing weather details
     */
    public WeatherInfo getCurrentWeather(double latitude, double longitude) throws IOException, URISyntaxException {
        System.out.println("Génération de données météo simulées pour lat=" + latitude + ", lon=" + longitude);
        
        try {
            // Simulation d'un court délai pour imiter un appel réseau
            Thread.sleep(200);
            
            // Choisir une condition météo aléatoire
            String condition = weatherConditions[random.nextInt(weatherConditions.length)];
            String description = generateDescription(condition);
            
            // Générer des valeurs météo plausibles
            double temperature = 15 + (random.nextDouble() * 20) - 10; // Entre 5 et 25°C
            int humidity = 40 + random.nextInt(60); // Entre 40 et 99%
            double windSpeed = random.nextDouble() * 10; // Entre 0 et 10 m/s
            String iconCode = getIconCodeForCondition(condition);
            
            WeatherInfo weatherInfo = new WeatherInfo(
                condition,
                description,
                temperature,
                humidity,
                windSpeed,
                iconCode
            );
            
            System.out.println("Données météo simulées générées: " + weatherInfo);
            return weatherInfo;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Erreur lors de la génération des données météo simulées");
        }
    }
    
    /**
     * Gets the forecast weather for multiple days
     * @param latitude location latitude
     * @param longitude location longitude
     * @param startDate start date for forecast
     * @param endDate end date for forecast
     * @return List of ForecastInfo objects containing forecast for each day
     */
    public List<ForecastInfo> getForecast(double latitude, double longitude, LocalDate startDate, LocalDate endDate) 
            throws IOException, URISyntaxException {
        
        System.out.println("Génération de prévisions météo simulées de " + startDate + " à " + endDate);
        List<ForecastInfo> forecasts = new ArrayList<>();
        
        try {
            // Simulation d'un court délai pour imiter un appel réseau
            Thread.sleep(300);
            
            // Générer une prévision pour chaque jour dans la plage de dates
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                // Générer des données météo aléatoires pour ce jour
                // 20% de chance d'avoir des conditions défavorables
                String condition;
                if (random.nextInt(100) < 20) {
                    condition = adverseConditions[random.nextInt(adverseConditions.length)];
                } else {
                    condition = weatherConditions[random.nextInt(weatherConditions.length)];
                }
                
                String description = generateDescription(condition);
                double temperature = 15 + (random.nextDouble() * 20) - 10;
                int humidity = 40 + random.nextInt(60);
                double windSpeed = random.nextDouble() * 10;
                String iconCode = getIconCodeForCondition(condition);
                
                ForecastInfo forecast = new ForecastInfo(
                    current,
                    condition,
                    description,
                    temperature,
                    humidity,
                    windSpeed,
                    iconCode
                );
                
                forecasts.add(forecast);
                current = current.plusDays(1);
            }
            
            return forecasts;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Erreur lors de la génération des prévisions météo simulées");
        }
    }
    
    /**
     * Checks if there are any adverse weather conditions in the date range
     * @param latitude location latitude
     * @param longitude location longitude
     * @param startDate start date to check
     * @param endDate end date to check
     * @return String description of adverse conditions or null if no adverse conditions
     */
    public String checkAdverseWeatherConditions(double latitude, double longitude, 
                                               LocalDate startDate, LocalDate endDate) 
            throws IOException, URISyntaxException {
        
        List<ForecastInfo> forecasts = getForecast(latitude, longitude, startDate, endDate);
        StringBuilder adverseConditions = new StringBuilder();
        
        for (ForecastInfo forecast : forecasts) {
            String condition = forecast.getCondition().toLowerCase();
            String description = forecast.getDescription().toLowerCase();
            
            if (isAdverseCondition(condition, description)) {
                if (adverseConditions.length() > 0) {
                    adverseConditions.append("\n");
                }
                adverseConditions.append(forecast.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                          .append(": ")
                          .append(forecast.getDescription())
                          .append(" (").append(Math.round(forecast.getTemperature())).append("°C)");
            }
        }
        
        return adverseConditions.length() > 0 ? adverseConditions.toString() : null;
    }
    
    /**
     * Determines if a weather condition is adverse
     */
    private boolean isAdverseCondition(String condition, String description) {
        return condition.contains("pluie") || condition.contains("orage") || 
               condition.contains("tempête") || condition.contains("neige") || 
               condition.contains("grêle") || condition.contains("brouillard") ||
               description.contains("forte") || description.contains("abondante") ||
               description.contains("violent");
    }
    
    /**
     * Generate a detailed description based on condition
     */
    private String generateDescription(String condition) {
        if (condition.contains("Ciel dégagé") || condition.contains("Ensoleillé")) {
            return "Temps ensoleillé";
        } else if (condition.contains("Nuageux")) {
            return "Ciel couvert";
        } else if (condition.contains("Partiellement")) {
            return "Éclaircies par moments";
        } else if (condition.contains("Pluie légère")) {
            return "Légères averses";
        } else if (condition.contains("Pluie modérée")) {
            return "Averses continues";
        } else if (condition.contains("Orage")) {
            return "Orages et pluies";
        } else if (condition.contains("Brouillard")) {
            return "Visibilité réduite";
        } else if (condition.contains("Neige")) {
            return "Chutes de neige";
        } else if (condition.contains("Grêle")) {
            return "Risque de grêle";
        } else if (condition.contains("Tempête")) {
            return "Vents violents et pluie";
        } else {
            return "Conditions variables";
        }
    }
    
    /**
     * Map condition to icon code
     */
    private String getIconCodeForCondition(String condition) {
        if (condition.contains("Ciel dégagé") || condition.contains("Ensoleillé")) {
            return "01d";
        } else if (condition.contains("Nuageux")) {
            return "04d";
        } else if (condition.contains("Partiellement")) {
            return "02d";
        } else if (condition.contains("Pluie")) {
            return "10d";
        } else if (condition.contains("Orage")) {
            return "11d";
        } else if (condition.contains("Brouillard")) {
            return "50d";
        } else if (condition.contains("Neige")) {
            return "13d";
        } else {
            return "02d"; // default
        }
    }
    
    /**
     * Inner class representing current weather information
     */
    public static class WeatherInfo {
        private final String condition;
        private final String description;
        private final double temperature;
        private final int humidity;
        private final double windSpeed;
        private final String iconCode;
        
        public WeatherInfo(String condition, String description, double temperature, 
                          int humidity, double windSpeed, String iconCode) {
            this.condition = condition;
            this.description = description;
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.iconCode = iconCode;
        }
        
        public String getCondition() {
            return condition;
        }
        
        public String getDescription() {
            return description;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public int getHumidity() {
            return humidity;
        }
        
        public double getWindSpeed() {
            return windSpeed;
        }
        
        public String getIconCode() {
            return iconCode;
        }
        
        public String getIconUrl() {
            return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        }
        
        @Override
        public String toString() {
            return condition + ": " + description + ", " + 
                   Math.round(temperature) + "°C, " + 
                   "Humidité: " + humidity + "%, " + 
                   "Vent: " + Math.round(windSpeed * 10) / 10.0 + " m/s";
        }
    }
    
    /**
     * Inner class representing forecast information for a specific date
     */
    public static class ForecastInfo extends WeatherInfo {
        private final LocalDate date;
        
        public ForecastInfo(LocalDate date, String condition, String description, 
                           double temperature, int humidity, double windSpeed, String iconCode) {
            super(condition, description, temperature, humidity, windSpeed, iconCode);
            this.date = date;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        @Override
        public String toString() {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ": " + super.toString();
        }
    }
} 