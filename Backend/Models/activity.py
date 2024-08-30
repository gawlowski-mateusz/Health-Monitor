from db import db


class ActivityModel(db.Model):
    __tablename__ = 'activities'

    activity_id = db.Column(db.Integer, nullable=False, primary_key=True)
    training_id = db.Column(db.Integer, nullable=False, foreign_key=True)
    steps_id = db.Column(db.Integer, nullable=False, foreign_key=True)
    goal_id = db.Column(db.Integer, nullable=False, foreign_key=True)
    user_id = db.Column(db.Integer, nullable=False, foreign_key=True)

