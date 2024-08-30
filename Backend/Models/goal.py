from db import db


class GoalModel(db.Model):
    __tablename__ = 'goals'

    goal_id = db.Column(db.Integer, primary_key=True)
    steps_count = db.Column(db.Integer, nullable=False)
