from typing import List
from pydantic import BaseModel,Field

class Skill(BaseModel):
    num: int
    dmg: int
    ratio: dict = Field(..., alias="ratio")
    cooldown: int
    lvlMax: int = Field(..., alias="lvlMax")

class MonstreModel(BaseModel):
    _id: int
    element: str
    hp: int
    atk: int
    defn: int = Field(..., alias="def")
    vit: int
    skills: List[Skill]
    lootRate: float

class UserModel(BaseModel):
    _id: str
    Id: str
    Level: str
    Exp: str
    Monstre: List[MonstreModel]

class CompteModel(BaseModel):
    _id: str
    Id: str
    Username: str
    address: str
    role : str
    hashed_password: str