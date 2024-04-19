package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIClient {

    private static final String API_URL = "http://apipython/token";
    String token ;

    public APIClient(String username, String password) throws Exception {
        token = getAccessToken(username, password);
    }

    private String getAccessToken(String username, String password) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Set up the request parameters
        String requestBody = "username=" + username + "&password=" + password + "&grant_type=password";

        // Set up headers
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Write the request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public static String check(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            return "Token d'accès manquant ou invalide.";
        }
    
        // URL de l'API /test
        String apiUrl = "http://apipython:80/checkToken/"+token;
    
        // Création d'un client HTTP
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
    
        // Création de la requête GET pour /test
        connection.setRequestMethod("GET");
    
        // Ajout du token d'accès dans l'en-tête de la requête
        connection.setRequestProperty("Authorization", "Bearer " + token);
    
        try {
            // Exécution de la requête
            int statusCode = connection.getResponseCode();
    
            // Vérification du code de statut
            if (statusCode == 200) {
                // Utilisation de Jackson pour parser la réponse JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(connection.getInputStream());
    
                // Récupération des valeurs d'ID et de l'username
                System.out.println(jsonResponse);
                String id = jsonResponse.path("message").path("id").asText();
                String username = jsonResponse.path("message").path("Username").asText();
    
                return "ID : " + id + " Username : " + username;
            } else {
                return "Erreur lors de l'appel de l'API /test. Code de statut : " + statusCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'appel de l'API /test. Exception : " + e.getMessage();
        } finally {
            connection.disconnect();
        }
    }
}
