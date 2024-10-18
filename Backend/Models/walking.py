from db import db


class WalkingModel(db.Model):
    __tablename__ = 'walking'

    walking_id = db.Column(db.Integer, primary_key=True)
    average_pulse = db.Column(db.Integer, nullable=False)
    duration = db.Column(db.Integer, nullable=False)
    date = db.Column(db.Date, nullable=False)

    training_id = db.Column(db.Integer, db.ForeignKey("training.training_id"), nullable=False)
    training = db.relationship('TrainingModel', back_populates='walking')

    def to_dict(self):
        return {
            "walking_id": self.walking_id,
            "average_pulse": self.average_pulse,
            "duration": self.duration,
            "date": self.date.strftime('%Y-%m-%d')  # Convert date to string for JSON compatibility
        }
