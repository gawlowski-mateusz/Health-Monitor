from db import db


class StepsModel(db.Model):
    __tablename__ = 'steps'

    steps_id = db.Column(db.Integer, primary_key=True)
    count = db.Column(db.Integer, nullable=False)
    goal = db.Column(db.Integer, nullable=False)
    date = db.Column(db.Date, nullable=False)

    activity = db.relationship('ActivityModel', back_populates='steps')

    def to_dict(self):
        return {
            "steps_id": self.steps_id,
            "count": self.count,
            "goal": self.goal,
            "date": self.date.strftime('%Y-%m-%d')  # Convert date to string for JSON compatibility
        }
