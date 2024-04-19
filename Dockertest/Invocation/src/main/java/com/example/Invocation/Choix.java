package com.example.Invocation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Choix {
    private String id;

    public int getId() {
        return Integer.parseInt(id);
    }

    public  String getProba() {
        // URL de l'API
        String apiUrl = "http://api:8080/api/monster/allLootRate";

        // Création d'un client HTTP
        HttpClient httpClient = HttpClients.createDefault();

        // Création de la requête GET
        HttpGet httpGet = new HttpGet(apiUrl);

        try {
            // Exécution de la requête
            HttpResponse response = httpClient.execute(httpGet);

            // Vérification du code de statut
            if (response.getStatusLine().getStatusCode() == 200) {
                // Récupération de la réponse JSON
                String jsonResponse = EntityUtils.toString(response.getEntity());
                return jsonResponse;
            } else {
                System.err.println("Erreur lors de l'appel de l'API. Code de statut : " + response.getStatusLine().getStatusCode());
                return String.valueOf(response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String setId() {
        String data = getProba();

        JSONArray jsonArray = new JSONArray(data);

        double randomValue = Math.random();
        double SommeProba = 0.0;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            double lootRate = object.getDouble("lootRate");

            SommeProba += lootRate;
            if (randomValue <= SommeProba) {
                this.id = object.getString("id");
                return id;
            }
        }
        return id;
    }
}
