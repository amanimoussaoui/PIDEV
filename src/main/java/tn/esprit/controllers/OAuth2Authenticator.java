package tn.esprit.controllers;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class OAuth2Authenticator {
    public static void main(String[] args) {
        try {
            // Define the client ID and client secret
            String clientId = "335142041387-ovfpk58kvejmraeckf3qm3bmh8tji7ku.apps.googleusercontent.com";
            String clientSecret = "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";  // Ensure this is retrieved securely

            // OAuth 2.0 authorization URL
            String authorizationUrl = "https://accounts.google.com/o/oauth2/auth?client_id=" + clientId +
                    "&redirect_uri=http://localhost&scope=https://www.googleapis.com/auth/userinfo.email openid&response_type=code";

            // Display the URL to obtain the authorization code
            System.out.println("Visit this URL to authorize the application and obtain the authorization code: ");
            System.out.println(authorizationUrl);

            // Prompt the user to paste the authorization code
            Scanner scanner = new Scanner(System.in);
            System.out.print("Paste the authorization code here: ");
            String authCode = scanner.nextLine();

            // Exchange the authorization code for an access token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            String postBody = "code=" + authCode +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&redirect_uri=http://localhost" +
                    "&grant_type=authorization_code";

            // Send the POST request to obtain the access token
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Analyze the response to extract the token
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String accessToken = jsonResponse.getString("access_token");
                System.out.println("Access Token: " + accessToken);

                // Use this token to make API calls
                String apiUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
                HttpRequest apiRequest = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();
                HttpResponse<String> apiResponse = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("API Response: " + apiResponse.body());
            } else {
                System.out.println("Error exchanging authorization code for token.");
                System.out.println("Error: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}