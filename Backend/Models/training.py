from db import db


class TrainingModel(db.Model):
    __tablename__ = 'trainings'

    training_id = db.Column(db.Integer, primary_key=True)
    training_date = db.Column(db.Date, nullable=False)
    average_heart_rate = db.Column(db.Float)
    duration = db.Column(db.Integer, nullable=False)