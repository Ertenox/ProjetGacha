FROM mongo:latest

WORKDIR /app
ADD data.json /app/data.json
ADD compte.json /app/compte.json

# Exécutez la commande mongoimport pour importer les données dans MongoDB lors du démarrage du conteneur
#CMD mongoimport --host localhost --db gacha --collection Monstre --type json --file data.json --jsonArray & mongoimport --host localhost --db gacha --collection Compte --type json --file compte.json --jsonArray

