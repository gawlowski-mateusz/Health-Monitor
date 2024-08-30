from db import db


class StepsModel(db.Model):
    __tablename__ = 'staps'

    steps_id = db.Column(db.Integer, primary_key=True)
    steps_date = db.Column(db.Date, nullable=False)
    count = db.Column(db.Integer, nullable=False)
