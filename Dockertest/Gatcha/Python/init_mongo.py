from auth import get_password_hash
from models import CompteModel
from test import *

new_user_data = CompteModel(
    Id="1",
    Username="Admin",
    address="admin@localhost",
    role="Admin",
    hashed_password= get_password_hash("Admin")
)

set_Compte(new_user_data)