from pymongo import MongoClient
import pprint
from fastapi import Depends, FastAPI, HTTPException
from models import *
from passlib.context import CryptContext
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Définition des paramètres de connexion
LIEN_CONNEXION = "mongodb://mongo:27017/"
DATABASE = "gacha"
# Création d'une connnexion à la base de données
client = MongoClient(LIEN_CONNEXION)
# Affichage des noms des bases de données disponibles
db = client[DATABASE]


app = FastAPI()

def get_AllDatabase():
   """Fonction pour obtenir les noms de toutes les bases de données disponibles"""
   print(client.list_database_names())
   return

def get_Collection():
    """Fonction pour obtenir les noms de toutes les collections disponibles"""
    print(db.list_collection_names())
    return

def get_AllUsers():
    """Fonction pour obtenir les informations de tous les utilisateurs"""
    collection = db["Users"]
    for user in collection.find():
       pprint.pprint(user)
    return

def get_AllCompte():
    """Fonction pour obtenir les informations d'un utilisateur"""
    collection = db["Compte"]
    for user in collection.find():
       pprint.pprint(user)
    return

def set_Compte(user_data: CompteModel):
    """Fonction pour ajouter un utilisateur"""
    collection = db["Compte"]
    user_dict = user_data.dict()
    inserted_user = collection.insert_one(user_dict)
    return str(inserted_user.inserted_id)

def get_password_hash(password):
    """Fonction pour obtenir le hash d'un mot de passe"""
    return pwd_context.hash(password)

# Appel des fonctions
#get_AllDatabase()
#get_Collection()
#get_AllUsers()
#get_Compte()


new_user_data = CompteModel(
    _id="222",
    Id="some_user_id",
    Username="new_user",
    address="some_address",
    role="user",
    hashed_password= get_password_hash("hashed_password")
)

#inserted_id = set_Compte(new_user_data)