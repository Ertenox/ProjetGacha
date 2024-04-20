package com.example.Invocation;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

}

@RestController
@RequestMapping(path = "/Invocation")
class InvocationController {

    @GetMapping(path = "/assignRandomMonster/{token}")
    public String assignRandomMonster(@PathVariable("token") String token) throws IOException {
        Choix choix = new Choix();
        choix.setId();
        URL urlUser = new URL("http://api:8080/api/assignMonster/" + token + "/" + choix.getId());
        HttpURLConnection con = (HttpURLConnection) urlUser.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    
        // Analyse de la réponse de l'API
        if (response.toString().equals("Monster assigned")) {
            return "Monster with id " + choix.getId() + " assigned to user.";
        } else {
            return "Response from API: " + response.toString();
        }
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
