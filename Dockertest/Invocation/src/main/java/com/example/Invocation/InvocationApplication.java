package com.example.Invocation;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@SpringBootApplication
public class InvocationApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvocationApplication.class, args);
	}

	public static String verifToken(String token) throws IOException{
		if (token == null) {
            return "Token d'accès manquant.";
        }
    
        // URL de l'API /test
        String apiUrl = "http://apipython/checkToken/"+token;
    
        // Création d'un client HTTP
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
    
        // Création de la requête GET pour /test
        connection.setRequestMethod("GET");
    
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
                String id = jsonResponse.path("message").path("Id").asText();
				System.out.println(id);
                return id;
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

@RestController
@RequestMapping(path = "/Invocation")
class InvocationController {

	@GetMapping(path = "/{token}/assignRandomMonster")
    public String assignRandomMonster(@PathVariable("token") String token) throws IOException {
        String idJoueur = InvocationApplication.verifToken(token);
		Choix choix = new Choix();
		choix.setId();
		System.out.println(choix.getId());
        URL urlUser = new URL("http://api:8080/api/assignMonster/"+idJoueur+"/"+choix.getId());
        HttpURLConnection con = (HttpURLConnection) urlUser.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        int status = con.getResponseCode();
        if (status == 200) {
            return "Monster assigned";
        } else {
            return "Monster not assigned";
        }
    }

	@GetMapping(path="/{token}/check")
	public String check(@PathVariable("token") String token) throws Exception{
		return InvocationApplication.verifToken(token);
	}

    @GetMapping(path="/testRandomMonster/{NbTest}")
    public String testRandomMonster(@PathVariable("NbTest") int NbTest) {
        // Liste pour stocker les occurrences de chaque chiffre
        List<Integer> occurrences = new ArrayList<>();
        int total = 0;
        
        // Générer les chiffres et compter les occurrences
        for (int i = 0; i < NbTest; i++) {
            Choix choix = new Choix();
            choix.setId();
            int chiffre = choix.getId();
            while (occurrences.size() <= chiffre) {
                occurrences.add(0);
            }
            occurrences.set(chiffre, occurrences.get(chiffre) + 1);
            total++;
        }
        String result = "";
        for (int chiffre = 0; chiffre < occurrences.size(); chiffre++) {
            int occurrence = occurrences.get(chiffre);
            if (occurrence == 0) {
                continue;
            }
            double pourcentage = (double) occurrence / total * 100;
            result += chiffre + ": " + pourcentage + "%\n";
        }
        
        return result;
    }
    

}
