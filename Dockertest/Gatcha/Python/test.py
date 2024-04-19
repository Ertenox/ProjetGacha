from fastapi.responses import RedirectResponse
from pymongo import MongoClient
import pprint
from fastapi import Depends, FastAPI, HTTPException
from bson import json_util
from models import *
from connexion_db import *
import auth
from auth import *

# Définition des paramètres de connexion
LIEN_CONNEXION = "mongodb://mongo:27017/"
DATABASE = "gacha"
# Création d'une connexion à la base de données
client = MongoClient(LIEN_CONNEXION)
# Affichage des noms des bases de données disponibles
db = client[DATABASE]

app = FastAPI()
app.include_router(auth.router)

def get_AllDatabase():
   """Fonction pour obtenir les noms de toutes les bases de données disponibles"""
   return client.list_database_names()

def get_Collection():
    """Fonction pour obtenir les noms de toutes les collections disponibles"""
    return db.list_collection_names()

def get_AllUsers():
    """Fonction pour obtenir les informations de tous les utilisateurs"""
    collection = db["Users"]
    return list(collection.find())

def get_AllMonsters():
    """Fonction pour obtenir les informations de tous les monstres"""
    collection = db["Monstre"]
    print(collection)
    return list(collection.find())


@app.get("/")
def read_root():
    return RedirectResponse(url="/docs/")


@app.post("/users")
def create_user(new_data: CompteModel):
    new_user_data = CompteModel(
        Id=new_data.Id,
        Username=new_data.Username,
        address=new_data.address,
        role=new_data.role,
        hashed_password=get_password_hash(new_data.hashed_password)
    )
    #Insert les données dans la base de données
    set_Compte(new_user_data)
    return

def format_user(user):
    return UserModel(
        _id=str(user['_id']),
        Id=user['Id'],
        Level=user['Level'],
        Exp=user['Exp'],
        Monstre=[MonstreModel(Nom=monstre['Nom']) for monstre in user['Monstre']]
    )

def format_monster(monstre):
    return MonstreModel(

    )



# Endpoints FastAPI
@app.get("/databases")
def read_all_databases():
    """Permet d'obtenir les noms de toutes les bases de données"""
    return get_AllDatabase()

@app.get("/collections")
def read_all_collections():
    """Permet d'obtenir les noms de toutes les collections dans une base de données spécifiée"""
    return get_Collection()

@app.get("/all_users", response_model=List[UserModel])
def read_all_users():
    """Permet d'obtenir les informations de tous les utilisateurs dans une collection spécifiée"""
    users = get_AllUsers()
    return [format_user(user) for user in users]

@app.get("/test")
def read_root(current_user = Depends(get_current_user)):
    """Fonction pour obtenir les informations de tous les utilisateurs"""
    collection = db["Compte"]
    print(current_user,"\n",collection.find_one({"Username": current_user["Username"]}))
    return {"message": current_user}

@app.get("/checkToken/{token}")
def checkToken(token: str):
    """Fonction pour verifier la validité d'un token"""
    print(token)
    str = get_current_user2(db, token)
    return {"message": str}

@app.get("/Monstre")
def read_monster():
    Monsters = get_AllMonsters()
    return Monsters

#def get_AllCompte(db: Session, username: str):
    """Fonction pour obtenir les informations d'un utilisateur"""
    collection = db["Compte"]
    for user in collection.find():
       pprint.pprint(user)
    return
