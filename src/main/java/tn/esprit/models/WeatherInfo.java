package tn.esprit.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherInfo {
    private double temperature;
    private String description;
    private int humidity;
    private double windSpeed;
    private int pressure;
    private int cloudCover;
    private LocalDateTime sunrise;
    private LocalDateTime sunset;
    private String iconUrl;
    private String city;
    private String country;

    // Constructeur par défaut
    public WeatherInfo() {
        this.description = "Information non disponible";
        this.city = "Ville inconnue";
        this.country = "Pays inconnu";
    }

    // Getters et Setters avec validation
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description != null ? description : "Information non disponible";
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "Information non disponible";
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity >= 0 && humidity <= 100 ? humidity : 0;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed >= 0 ? windSpeed : 0;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure > 0 ? pressure : 1013; // valeur standard de la pression atmosphérique
    }

    public int getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(int cloudCover) {
        this.cloudCover = cloudCover >= 0 && cloudCover <= 100 ? cloudCover : 0;
    }

    public LocalDateTime getSunrise() {
        return sunrise;
    }

    public void setSunrise(LocalDateTime sunrise) {
        this.sunrise = sunrise;
    }

    public LocalDateTime getSunset() {
        return sunset;
    }

    public void setSunset(LocalDateTime sunset) {
        this.sunset = sunset;
    }

    public String getIconUrl() {
        return iconUrl != null ? iconUrl : "http://openweathermap.org/img/w/01d.png"; // icône par défaut
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCity() {
        return city != null ? city : "Ville inconnue";
    }

    public void setCity(String city) {
        this.city = city != null ? city : "Ville inconnue";
    }

    public String getCountry() {
        return country != null ? country : "Pays inconnu";
    }

    public void setCountry(String country) {
        this.country = country != null ? country : "Pays inconnu";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCity()).append(", ").append(getCountry()).append(" - ");
        sb.append(getDescription()).append(": ");
        sb.append(String.format("%.1f°C", getTemperature()));
        
        if (getHumidity() > 0) {
            sb.append(", Humidité: ").append(getHumidity()).append("%");
        }
        if (getWindSpeed() > 0) {
            sb.append(", Vent: ").append(String.format("%.1f m/s", getWindSpeed()));
        }
        if (getPressure() > 0) {
            sb.append(", Pression: ").append(getPressure()).append(" hPa");
        }
        if (getCloudCover() > 0) {
            sb.append(", Nuages: ").append(getCloudCover()).append("%");
        }
        
        if (getSunrise() != null && getSunset() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            sb.append("\nLever du soleil: ").append(getSunrise().format(formatter));
            sb.append(", Coucher du soleil: ").append(getSunset().format(formatter));
        }
        
        return sb.toString();
    }
} 