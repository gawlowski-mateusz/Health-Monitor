from db import db


class TrainingModel(db.Model):
    __tablename__ = 'trainings'

    training_id = db.Column(db.Integer, primary_key=True)
    average_heart_rate = db.Column(db.Float, nullable=True)
    duration = db.Column(db.Integer, nullable=False)

    activity = db.relationship('ActivityModel', back_populates='trainings')
