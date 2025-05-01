package tn.esprit.services;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class CurrencyConverter {
    private static final String API_KEY = "07019c286857a062b5585f2f"; // ⚡ Mets ta vraie clé ici
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    public static double getExchangeRate(String fromCurrency, String toCurrency) throws Exception {
        String urlStr = BASE_URL + API_KEY + "/pair/" + fromCurrency + "/" + toCurrency;
        URL url = new URL(urlStr);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        Scanner scanner = new Scanner(new InputStreamReader(request.getInputStream()));
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        JSONObject obj = new JSONObject(response);
        if (obj.getString("result").equals("success")) {
            return obj.getDouble("conversion_rate");
        } else {
            throw new RuntimeException("Erreur d'obtention du taux de change : " + obj.getString("error-type"));
        }
    }
}
