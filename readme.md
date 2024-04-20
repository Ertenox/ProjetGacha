Une fois le Docker Compose lancé, il est nécessaire d'initialiser la base de données avec des comptes ainsi que des monstres.
Utilisez les commandes suivantes pour importer les données JSON dans la base de données MongoDB :

mongoimport --host localhost --db gacha --collection Monstre --type json --file data.json --jsonArray & mongoimport --host localhost --db gacha --collection Compte --type json --file compte.json --jsonArray

### API CLIENT ###
Pour toutes les API, voici les liens de test :

- Connexion : `http://localhost:8080/api/connexion/{username}/{password}`
  - Comptes créés par défaut :
    - Joueur 1 : http://localhost:8080/api/connexion/Joueur1/Joueur1 -> compté conseiller pour la suite des tests
    - Joueur 2 : http://localhost:8080/api/connexion/Joueur2/Joueur2
  - Le joueur 1 possède par défaut le monstre d'id 1 (plus rapide pour dérouler les tests)
  - Le joueur 2 possède déja 10 monstre attribué de manière statique.

Une fois connecté, récupérez votre token d'authentification.

- Vérification du token : `http://localhost/checkToken/{token}`
  - Résultat attendu : un JSON contenant toutes les informations relatives au compte.
  - A cette étape, il est nécessaire de récupéré le token pour les liens suivants.
- Affichage d'un monstre par ID : `http://localhost:8080/api/monster/{id}`
  - Test : http://localhost:8080/api/monster/1

- Affichage des probabilités d'apparition de chaque monstre : `http://localhost:8080/api/monster/allLootRate`

- Ajout d'un monstre au joueur : `http://localhost:8080/api/assignMonster/{token}/{idMonster}`

- Suppression d'un monstre du joueur : `http://localhost:8080/api/removeMonster/{token}/{idMonster}`

- Ajout d'XP au joueur : `http://localhost:8080/api/addXP/{token}/{xp}`

### API INVOCATION ###

- Test de la répartition de la fonction aléatoire : `http://localhost:8081/Invocation/testRandomMonster/{NbTest}`
  - Exemple : http://localhost:8081/Invocation/testRandomMonster/500 (une répartition proche de 30-30-30-10 est attendue).

- Piocher un monstre aléatoire en contactant l'API client : http://localhost:8081/Invocation/assignRandomMonster/{token}

### API COMBAT ###

Cette API vérifie les différentes données saisies et, si elles sont valides, exécute le combat. Les logs sont affichés sur la page web et enregistrés dans un fichier de logs `/apps/logs/combatLog+[temps en millisecondes].txt`.

- Démarrer un combat : `http://localhost:8082/Combat/start/{token}/{idJoueurCible}/{idMonstrePossede}/{idMonstreCible}`
  - Exemple si connexion avec le joueur 1 : http://localhost:8082/Combat/start/{token}/2/4/1