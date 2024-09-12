from db import db


class CyclingModel(db.Model):
    __tablename__ = 'cycling'

    cycling_id = db.Column(db.Integer, primary_key=True)
    average_pulse = db.Column(db.Integer, nullable=False)
    duration = db.Column(db.Integer, nullable=False)
    date = db.Column(db.Date, nullable=False)

    training_id = db.Column(db.Integer, db.ForeignKey("training.training_id"), nullable=False)
    training = db.relationship('TrainingModel', back_populates='cycling')
