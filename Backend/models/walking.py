from db import db


class WalkingModel(db.Model):
    __tablename__ = 'walking'

    walking_id = db.Column(db.Integer, primary_key=True)
    average_pulse = db.Column(db.Float, nullable=False)
    date = db.Column(db.Date, nullable=False)

    training = db.relationship('TrainingModel', back_populates='walking')
