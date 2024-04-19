une fois le docker compose, il faut initialiser la base de donné avec un premier compte ainsi qu'avec les pals
    
mongoimport --host localhost --db gacha --collection Monstre --type json --file data.json --jsonArray & mongoimport --host localhost --db gacha --collection Compte --type json --file compte.json --jsonArray


### API CLIENT ### Pour toutes les API 
Liens de test :

http://localhost:8080/api/connexion/{username}/{password} -> permet de se connecter à un compte
    test : http://localhost:8080/api/connexion/Admin/Admin Compte créer lors de l'init

ensuite récupérer le token Voici votre Token d'authentification 

http://localhost/checkToken/{token} -> permet de tester la validité du token
    resultat attendu : un json contenant toutes mes information remative au compte

http://localhost:8080/api/monster/{id} -> affiche le monstre avec l'id saisie
    test : http://localhost:8080/api/monster/1

http://localhost:8080/api/monster/allLootRate -> affiche la probabilité de chaque monstre d'apparaitre

http://localhost:8080/api/assignMonster/{token}/{idMonster} -> permet d'ajouter le monstre au joueur ayant le token
http://localhost:8080/api/removeMonster/{token}/{idMonster} -> permet de supprimer le monstre au joueur ayant le token
http://localhost:8080/api/addXP/{token}/{xp} -> permet de s'ajouter de l'XP afin d'augmenter sa capacité de monstre 

### API INVOCATION ###

http://api:8081/Invocation/testRandomMonster/{NbTest} -> permet dester la repartition de la fonction aléatoire 
    http://api:8081/Invocation/testRandomMonster/500 : une repation proche de 30 30 30 10 est attendu

http://localhost:8081/Invocation/{token} -> permet d'ajouter un monstre à l'utilisateur avec le token choisi