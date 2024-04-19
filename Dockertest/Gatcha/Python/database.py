from pymongo import MongoClient
# Définition des paramètres de connexion
LIEN_CONNEXION = "mongodb://mongo:27017/"
DATABASE = "gacha"

# Fonction pour obtenir la connexion à la base de données
def get_db():
    try:
        # Création d'une connnexion à la base de données
        client = MongoClient(LIEN_CONNEXION)
        # Affichage des noms des bases de données disponibles
        db = client[DATABASE]
        yield db
    finally:
        # Fermeture de la connexion à la base de données
        client.close()
