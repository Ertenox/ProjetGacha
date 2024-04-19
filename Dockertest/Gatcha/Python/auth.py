from jose import jwt, JWTError
from passlib.context import CryptContext
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi. security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from fastapi import Depends, APIRouter
from sqlalchemy.orm import Session
import datetime
import pprint
from pymongo import MongoClient
from pymongo import database
from bson.objectid import ObjectId
from database import get_db
import hashlib
import json


SECRET_KEY = "09d25e094faa6ca2556c818166b7a9563b93f7099f6fOf4caa6cf63b88e8d3e7"
ALGORITHM = "HS256"
#admin admin

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

router = APIRouter()

def get_password_hash(plain_password):
    """Fonction pour hasher le mot de passe"""
    return hashlib.sha256(plain_password.encode('utf-8')).hexdigest()

def verify_password(plain_password, hashed_password):
    """Fonction pour verifier si le mot de passe fourni correspond au mot de passe hashé"""
    return get_password_hash(plain_password) == hashed_password

def verifytoken(token):
    return


def get_Compte(db: database, username : str):
    """Fonction pour obtenir les informations d'un utilisateur dans la collection 'Compte'"""
    # Recherche l'utilisateur par nom d'utilisateur dans la collection 'Compte'
    Compte_data = db["Compte"].find_one({"Username": username})

    if Compte_data:
        # Convertit l'ID de type ObjectId en chaîne pour la compatibilité JSON
        Compte_data["_id"] = str(Compte_data["_id"])

    return Compte_data

# Function used to get user's info


def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    """Fonction utilisée pour obtenir les informations de l'utilisateur actuel"""

    try:
        # Charger le token JSON et extraire la valeur associée à "access_token"
        token_data = json.loads(token)
        access_token = token_data.get("access_token", "")
        
        decoded_token = jwt.decode(access_token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = decoded_token.get("sub")
        user = get_Compte(db, username)
        dead_time = decoded_token.get("dead_time")
        
        if dead_time is not None and datetime.datetime.utcnow() > datetime.datetime.fromisoformat(dead_time):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                                detail="Token expired",
                                headers={"WWW-Authenticate": "Bearer"})
        
        if user is None:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                                detail="Could not validate credentials",
                                headers={"WWW-Authenticate": "Bearer"})
        
        return user
    except (JWTError, json.JSONDecodeError) as e:
        print(str(e), "\n", token)
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail=f"Invalid token: {str(e)}",
                            headers={"WWW-Authenticate": "Bearer"})
    
def get_current_user2(db: database, token: str = Depends(oauth2_scheme)):
    """Fonction utilisée pour obtenir les informations de l'utilisateur actuel"""
    
    print("\n\n\n", token, "\n\n\n")

    try:
        
        decoded_token = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = decoded_token.get("sub")
        user = get_Compte(db, username)
        dead_time = decoded_token.get("dead_time")
        
        if dead_time is not None and datetime.datetime.utcnow() > datetime.datetime.fromisoformat(dead_time):
            print("token expiré")
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                                detail="Token expired",
                                headers={"WWW-Authenticate": "Bearer"})
        
        if user is None:
            print("User is none")
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                                detail="Could not validate credentials",
                                headers={"WWW-Authenticate": "Bearer"})
        
        return user
    except (JWTError, json.JSONDecodeError) as e:
        print(str(e), "\n", token)
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail=f"Invalid token: {str(e)}",
                            headers={"WWW-Authenticate": "Bearer"})


# A new route used for login 
@router.post("/token") 
def login(form_data: OAuth2PasswordRequestForm = Depends(), db : database = Depends(get_db)) : 
    """Nouvelle route utilisée pour la connexion, prend en paramètre les données du formulaire et la base de données. Retourne un token d'accès si les informations sont correctes"""
    user = get_Compte(db, form_data.username) 
    if not user or not verify_password(form_data.password, user["hashed_password"]) :
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Incorrect username or password",
                            headers={"WWW-Authenticate":"Bearer"}) #Retourne une erreur si le nom d'utilisateur ou le mot de passe est incorrect
    expiration_time = datetime.datetime.utcnow() + datetime.timedelta(minutes=20)#Crée un temps d'expiration pour le token
    data_to_encode = {"sub": user["Username"], "dead_time": expiration_time.isoformat()} #Crée un dictionnaire avec le nom d'utilisateur et le temps d'expiration
    print(expiration_time,"\n",expiration_time.isoformat())
    # Utilise un en-tête JSON standard
    access_token = jwt.encode(data_to_encode, SECRET_KEY, algorithm=ALGORITHM)  # Encode le token
    return {"access_token": access_token, "token_type": "bearer"}

 