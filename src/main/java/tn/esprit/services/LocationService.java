package tn.esprit.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * Service for fetching location information based on IP address
 */
public class LocationService {
    
    private static final String LOCATION_API_URL = "http://ip-api.com/json/";
    
    /**
     * Gets the location information based on the client's IP address
     * @return LocationInfo object containing coordinates and location details
     * @throws IOException if there's an error fetching location data
     */
    public LocationInfo getLocation() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(LOCATION_API_URL);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                    
                    if (jsonObject.has("status") && "success".equals(jsonObject.get("status").getAsString())) {
                        return new LocationInfo(
                            jsonObject.get("lat").getAsDouble(),
                            jsonObject.get("lon").getAsDouble(),
                            jsonObject.get("city").getAsString(),
                            jsonObject.get("country").getAsString(),
                            jsonObject.get("regionName").getAsString()
                        );
                    } else {
                        throw new IOException("Failed to get location: " + jsonObject.get("message").getAsString());
                    }
                }
                throw new IOException("No response from location API");
            }
        }
    }
    
    /**
     * Inner class representing location information
     */
    public static class LocationInfo {
        private final double latitude;
        private final double longitude;
        private final String city;
        private final String country;
        private final String region;
        
        public LocationInfo(double latitude, double longitude, String city, String country, String region) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.city = city;
            this.country = country;
            this.region = region;
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public String getCity() {
            return city;
        }
        
        public String getCountry() {
            return country;
        }
        
        public String getRegion() {
            return region;
        }
        
        @Override
        public String toString() {
            return city + ", " + region + ", " + country;
        }
    }
} 