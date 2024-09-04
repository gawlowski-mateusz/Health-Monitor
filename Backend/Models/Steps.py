from db import db


class StepsModel(db.Model):
    __tablename__ = 'steps'

    steps_id = db.Column(db.Integer, primary_key=True)
    count = db.Column(db.Integer, nullable=False)
    goal = db.Column(db.Integer, nullable=False)
    date = db.Column(db.Date, nullable=False)

    activity = db.relationship('ActivityModel', back_populates='steps')
