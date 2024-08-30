from db import db


class UserModel(db.Model):
    __tablename__ = 'users'

    user_id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(50), unique=True, nullable=False)
    password = db.Column(db.String(50), nullable=False)
    name = db.Column(db.String(50), nullable=False)
    sex = db.Column(db.String(20), nullable=True)
    weight = db.Column(db.Float, nullable=True)
    height = db.Column(db.Integer, nullable=True)
