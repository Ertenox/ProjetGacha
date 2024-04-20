package com.example.combat;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.*;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import java.io.*;
import java.net.*;
import java.util.List;
@SpringBootApplication

public class APICombat {
    
    public static void main(String[] args) {
        SpringApplication.run(APICombat.class, args);
    }

    public static MongoCollection<Document> getMongo(String collectionName) {
        MongoClient mongoClient = MongoClients.create("mongodb://mongo:27017");
        MongoDatabase database = mongoClient.getDatabase("gacha");
        return database.getCollection(collectionName);
    }

    public static String getIdByToken(String token) throws Exception {
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
    
                return  id ;
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

    public static boolean playerExists(int idJoueur) {
        MongoCollection<Document> collection = getMongo("Compte");
        Document joueur = collection.find(Filters.eq("id", idJoueur)).first();
        return joueur != null;
    }

    public static boolean monsterInPossession(int idJoueur, int idMonstre) {
        MongoCollection<Document> collection = getMongo("Compte");
        Document monstrePossede = collection.find(Filters.and(Filters.eq("id", idJoueur), Filters.elemMatch("Monstre", Filters.eq("idMonstre", idMonstre)))).first();
        return monstrePossede != null;
    }

    public static String verifStart(int idJoueur, int idJoueurCible, int idMonstrePossede, int idMonstreCible) {
        if (monsterInPossession(idJoueur, idMonstrePossede)){
            if (playerExists(idJoueurCible)) {
                if (monsterInPossession(idJoueurCible, idMonstreCible)) {
                    return "Combat lancé";
                } else {
                    return "Le joueur cible ne possède pas le monstre cible";
                }
            } else {
                return "Le joueur cible n'existe pas";
            }
        } else {
            return "Le joueur attaquant ne possède pas le monstre";
        }
    }

    public static int getCooldown(int idMonstre, int numSkill) {
        MongoCollection<Document> collection = getMongo("Monstre");
        Document monstre = collection.find(Filters.eq("id", idMonstre)).first();
        List<Document> skills = monstre.getList("skills", Document.class);
        for (Document skill : skills) {
            int num = skill.getInteger("num");
            if (num == numSkill) {
                return skill.getInteger("cooldown");
            }
        }
        return -1;
    }

    public static int getDamage(int idMonstre, int numSkill) {
        MongoCollection<Document> collection = getMongo("Monstre");
        Document monstre = collection.find(Filters.eq("id", idMonstre)).first();
        List<Document> skills = monstre.getList("skills", Document.class);
        for (Document skill : skills) {
            int num = skill.getInteger("num");
            if (num == numSkill) {
                return skill.getInteger("dmg");
            }
        }
        return -1;
    }

    public static int getHp(int idMonstre) {
        MongoCollection<Document> collection = getMongo("Monstre");
        Document monstre = collection.find(Filters.eq("id", idMonstre)).first();
        return monstre.getInteger("hp");
    }

    public static Boolean checkMort(int hp) {
        if (hp <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String combat(int idJoueur, int idJoueurCible, int idMonstrePossede, int idMonstreCible) {
        StringBuilder combatLog = new StringBuilder();
        int cooldownJ1S1 = 0;
        int cooldownJ1S2 = 0;
        int cooldownJ1S3 = 0;
        int cooldownJ2S1 = 0;
        int cooldownJ2S2 = 0;
        int cooldownJ2S3 = 0;
    
        int hpM1 = getHp(idMonstrePossede);
        int hpM2 = getHp(idMonstreCible);
    
        int dmgJ1S3 = getDamage(idMonstrePossede, 3);
        int dmgJ1S2 = getDamage(idMonstrePossede, 2);
        int dmgJ1S1 = getDamage(idMonstrePossede, 1);
        int dmgJ2S3 = getDamage(idMonstreCible, 3);
        int dmgJ2S2 = getDamage(idMonstreCible, 2);
        int dmgJ2S1 = getDamage(idMonstreCible, 1);
    
        String Name = "combatLog" + System.currentTimeMillis() + ".txt";
        File file = new File("logs/"+Name);
    
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            int T = 1;
    
            combatLog.append("Début du combat entre Monstre 1 et Monstre 2<br>");
            combatLog.append("Points de vie initiaux pour Monstre 1 : ").append(hpM1).append("<br>");
            combatLog.append("Points de vie initiaux pour Monstre 2 : ").append(hpM2).append("<br>");
    
            writer.println("Début du combat entre Monstre 1 et Monstre 2");
            writer.println("Points de vie initiaux pour Monstre 1 : " + hpM1);
            writer.println("Points de vie initiaux pour Monstre 2 : " + hpM2);
    
            while (true) {
                // Actions du Monstre 1
                if (cooldownJ1S3 == 0) {
                    String action = "T" + T + " : Monstre 1 utilise compétence 3 sur Monstre 2<br>" +
                                    "   - Dégâts infligés à Monstre 2 : " + dmgJ1S3 + "<br>" +
                                    "   - Points de vie restants pour Monstre 2 : " + (hpM2 -= dmgJ1S3) + "<br>";
                    combatLog.append(action);
                    writer.println(action);
                    cooldownJ1S3 = getCooldown(idMonstrePossede, 3);
                } else if (cooldownJ1S2 == 0) {
                    String action = "T" + T + " : Monstre 1 utilise compétence 2 sur Monstre 2<br>" +
                                    "   - Dégâts infligés à Monstre 2 : " + dmgJ1S2 + "<br>" +
                                    "   - Points de vie restants pour Monstre 2 : " + (hpM2 -= dmgJ1S2) + "<br>";
                    combatLog.append(action);
                    writer.println(action);
                    cooldownJ1S2 = getCooldown(idMonstrePossede, 2);
                } else if (cooldownJ1S1 == 0) {
                    String action = "T" + T + " : Monstre 1 utilise compétence 1 sur Monstre 2<br>" +
                                    "   - Dégâts infligés à Monstre 2 : " + dmgJ1S1 + "<br>" +
                                    "   - Points de vie restants pour Monstre 2 : " + (hpM2 -= dmgJ1S1) + "<br>";
                    combatLog.append(action);
                    writer.println(action);
                    cooldownJ1S1 = getCooldown(idMonstrePossede, 1);
                }
    
                if (checkMort(hpM2)) {
                    String action = "Monstre 2 a été vaincu!<br>";
                    combatLog.append(action);
                    writer.println(action);
                    break;
                }
    
                // Actions du Monstre 2
                if (cooldownJ2S3 == 0) {
                    String action = "T" + T + " : Monstre 2 utilise compétence 3 sur Monstre 1<br>" +
                                    "   - Dégâts infligés à Monstre 1 : " + dmgJ2S3 + "<br>" +
                                    "   - Points de vie restants pour Monstre 1 : " + (hpM1 -= dmgJ2S3) + "<br>";
                    combatLog.append(action);
                    writer.println(action);
                    cooldownJ2S3 = getCooldown(idMonstreCible, 3);
                } else if (cooldownJ2S2 == 0) {
                    String action = "T" + T + " : Monstre 2 utilise compétence 2 sur Monstre 1<br>" +
                                    "   - Dégâts infligés à Monstre 1 : " + dmgJ2S2 + "<br>" +
                                    "   - Points de vie restants pour Monstre 1 : " + (hpM1 -= dmgJ2S2) + "<br>";
                    combatLog.append(action);
                    writer.println(action);
                    cooldownJ2S2 = getCooldown(idMonstreCible, 2);
                } else if (cooldownJ2S1 == 0) {
                    String action = "T" + T + " : Monstre 2 utilise compétence 1 sur Monstre 1<br>" +
                                    "   - Dégâts infligés à Monstre 1 : " + dmgJ2S1 + "<br>" +
                                    "   - Points de vie restants pour Monstre 1 : " + (hpM1 -= dmgJ2S1) + "<br>";
                    combatLog.append(action);
                    writer.println(action);
                    cooldownJ2S1 = getCooldown(idMonstreCible, 1);
                }
    
                if (checkMort(hpM1)) {
                    String action = "Monstre 1 a été vaincu!<br>";
                    combatLog.append(action);
                    combatLog.append("Vous pouvez retouver les logs du combat dans le fichier /app/logs/" + Name);
                    writer.println(action);
                    break;
                }

                cooldownJ1S1 = Math.max(0, cooldownJ1S1 - 1);
                cooldownJ1S2 = Math.max(0, cooldownJ1S2 - 1);
                cooldownJ1S3 = Math.max(0, cooldownJ1S3 - 1);
                cooldownJ2S1 = Math.max(0, cooldownJ2S1 - 1);
                cooldownJ2S2 = Math.max(0, cooldownJ2S2 - 1);
                cooldownJ2S3 = Math.max(0, cooldownJ2S3 - 1);

                T++; 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return combatLog.toString();
    }
}


@RestController
@RequestMapping(path = "/Combat")
class CombatController {

    @GetMapping(path = "/start/{token}/{idJoueurCible}/{idMonstrePossede}/{idMonstreCible}")
    public String startCombat(@PathVariable("token") String token, @PathVariable("idJoueurCible") int idJoueurCible, @PathVariable("idMonstrePossede") int idMonstrePossede, @PathVariable("idMonstreCible") int idMonstreCible) throws NumberFormatException, Exception {
        int idJoueur;
        try {
            idJoueur = Integer.parseInt(APICombat.getIdByToken(token));
        } catch (NumberFormatException e) {
            return "Erreur : Token d'accès non valide";
        }
        if (APICombat.verifStart(idJoueur, idJoueurCible, idMonstrePossede, idMonstreCible).equals("Combat lancé")) {
            return APICombat.combat(idJoueur, idJoueurCible, idMonstrePossede, idMonstreCible);
        } else {
            return "Erreur : " + APICombat.verifStart(idJoueur, idJoueurCible, idMonstrePossede, idMonstreCible);
        }
         
    }

}


