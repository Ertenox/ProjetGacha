package com.example;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@SpringBootApplication
public class GatchaApplication {
    static final String dbName = "gacha";
    
    public static void main(String[] args) {
        SpringApplication.run(GatchaApplication.class, args);
    }

    public static MongoCollection<Document> getMongo(String collectionName) {
        MongoClient mongoClient = MongoClients.create("mongodb://mongo:27017");
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection(collectionName);  
    }

    public static boolean checkLevelUp(int idJoueur) {
        MongoCollection<Document> collection = GatchaApplication.getMongo("Compte");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
        if (xp >= (int) (Math.pow(1.1, level) * 50)) {
            levelUp(idJoueur);
            return true;
        } else return false;

    }

    public static void levelUp(int idJoueur) {
        MongoCollection<Document> collection = GatchaApplication.getMongo("Compte");
        //convert idJoueur to string
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
        int newLevel = level + 1;
        int newXP = xp - (int) (Math.pow(1.1, level) * 50);
        Document update = new Document("lvl", newLevel).append("exp", newXP).append("sizeMonster", doc.getInteger("sizeMonster") + 1);
        collection = GatchaApplication.getMongo("Compte");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));


    }

    public static void addXP(int idJoueur, int xp) {
        MongoCollection<Document> collection = GatchaApplication.getMongo("Compte");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int newXP = doc.getInteger("exp") + xp;
        Document update = new Document("exp", newXP);
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));
    }

    public static Document getInfo(int idJoueur) {
        MongoCollection<Document> collection = GatchaApplication.getMongo("Compte");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        return doc;
    }

    public static boolean addMonster(int idJoueur, int idMonstre) {
        //si on veut ajouter un monstre mais que la taille de l'équipe est déjà au max on ne peut pas
        MongoCollection<Document> collectionCompte = GatchaApplication.getMongo("Compte");
        Document doc = collectionCompte.find(Filters.eq("id", idJoueur)).first();
        if (doc.getInteger("sizeMonster") >= doc.getInteger("lvl") + 10) {
            System.err.println("Impossible d'ajouter un monstre car la taille de l'équipe est déjà au max");
            return false;
        } else {
            collectionCompte.updateOne(Filters.eq("id", idJoueur), new Document("$push", new Document("Monstre", new Document("id", idMonstre))));
            return true;
        }
    }

    public static boolean removeMonster(int idJoueur, int idMonstre) {
        MongoCollection<Document> collection = GatchaApplication.getMongo("Compte");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        if (doc.getInteger("sizeMonster") <= 0) {
            return false;
        } else {
            collection = GatchaApplication.getMongo("Compte");
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$pull", new Document("Monstre", new Document("id", idMonstre))));
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", new Document("sizeMonster", doc.getInteger("sizeMonster") - 1)));
            return true;
        }
    }

    public static String check(@RequestParam(name = "count", required = false) Integer count, String token) throws Exception {
        String response = APIClient.check(token);
        // Récupération des valeurs d'ID et de l'username en fonction du paramètre count;
        String[] words = response.split("\\s+");  
        if (words.length > 0) {
            if (count != null && (count == 1 || count == 2)){
                if (count == 1){
                    return words[count + 1];}
                if (count == 2)
                    return words[count + 3];
                else
                    return null;
            } else {
                return response;
            }
        } else {
            return "Réponse vide";  
        }
    }
}

@RestController
@RequestMapping("/api")
class GatchaApplicationController {

    APIClient client;

    @GetMapping("/")
    public  String afficheDB() {
        // URL de l'API
        String apiUrl = "http://apipython:80/databases";

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

    @GetMapping(path = "/connexion/{username}/{password}")
    public String getToken(@PathVariable("username") String username, @PathVariable("password") String password) {
        try {
            client = new APIClient(username, password);
            return "Voici votre Token d'authentification"+client.token;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la connexion : " + e.getMessage();
        }
    }

    @GetMapping(path = "/monster/{id}")
    public String getMonster(@PathVariable("id") String id) {
        int idnum = Integer.parseInt(id);
        MongoCollection<Document> collection = GatchaApplication.getMongo("Monstre");
        Document doc = collection.find(Filters.eq("id", idnum)).first();
        if (doc == null) {
            return "Monster not found";
        }
        return doc.toJson();
    }

    @GetMapping(path = "/monster/allLootRate")
    public String getAllLootRate() {
        MongoCollection<Document> collection = GatchaApplication.getMongo("Monstre");
        MongoCursor<Document> cursor = collection.find().iterator();

        StringBuilder result = new StringBuilder("[");
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String id = doc.get("id").toString();
            String lootRate = doc.get("lootRate").toString();
            result.append("{\"id\":\"").append(id).append("\",\"lootRate\":\"").append(lootRate).append("\"},");
        }

        if (result.charAt(result.length() - 1) == ',') {
            result.deleteCharAt(result.length() - 1); // Supprimer la virgule finale si elle existe
        }

        result.append("]");

        return result.toString();
    
    }
    
    @GetMapping(path = "/assignMonster/{token}/{idMonster}")
    public String assignMonster(@PathVariable("idMonster") String idMonster, @PathVariable("token") String token) throws Exception {
        int idJoueur = Integer.parseInt(GatchaApplication.check(1, token));
        int idMonsterNum = Integer.parseInt(idMonster);
        MongoCollection<Document> collectionCompte = GatchaApplication.getMongo("Compte");
        Document doc = collectionCompte.find(Filters.eq("id", idJoueur)).first();
        if (doc == null) {
            return "User not found";
        }
        MongoCollection<Document> collectionMonster = GatchaApplication.getMongo("Monstre");
        Document docMonster = collectionMonster.find(Filters.eq("id", idMonsterNum)).first();
        if (docMonster == null) {
            return "Monster not found";
        }
        return GatchaApplication.addMonster(idJoueur, idMonsterNum) ? "Monster assigned" : "Monster not assigned";
    }

    @GetMapping("/removeMonster/{token}/{idMonster}")
    public String removeMonster( @PathVariable("idMonster") String idMonstre, @PathVariable("token") String token) throws Exception {
        int idJoueur = Integer.parseInt(GatchaApplication.check(1, token));
        Document doc = GatchaApplication.getInfo(idJoueur);     
        //tester si l'user existe
        if (doc == null) {
            return "Impossible de retirer un monstre car l'id du joeur n'est pas valide";
        }
        if (Integer.parseInt(idMonstre) == 0) {
            return "Impossible de retirer un monstre car l'id n'est pas valide";
        } else {

            if (GatchaApplication.removeMonster(idJoueur, Integer.parseInt(idMonstre))) {
                return "Monstre retiré, nombre de monstres actuels :" + doc.getInteger("sizeMonster");
            } else {
                return "Impossible de retirer un monstre car vous n'en avez pas";
            }
        }
    }

    @GetMapping("/addXP/{token}/{xp}")
    public String addXP( @PathVariable("token") String token, @PathVariable("xp") int xp) throws Exception {
        int idJoueur = Integer.parseInt(GatchaApplication.check(1, token));
        GatchaApplication.addXP(idJoueur, xp);
        Document doc = GatchaApplication.getInfo(idJoueur);
        Thread.sleep(500);
        if(GatchaApplication.checkLevelUp(idJoueur)){
            return "XP ajouté, level up, nouveau level : " + doc.getInteger("lvl");
            }
        else {
            return "XP ajouté, pas de level up, level actuel : " + doc.getInteger("lvl");
        }
    }

}
