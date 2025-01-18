from db import db


class RunningModel(db.Model):
    __tablename__ = 'running'

    running_id = db.Column(db.Integer, primary_key=True)
    average_pulse = db.Column(db.Integer, nullable=False)
    duration = db.Column(db.Integer, nullable=False)
    date = db.Column(db.Date, nullable=False)

    training_id = db.Column(db.Integer, db.ForeignKey("training.training_id"), nullable=False)
    training = db.relationship('TrainingModel', back_populates='running')

    def to_dict(self):
        return {
            "running_id": self.running_id,
            "average_pulse": self.average_pulse,
            "duration": self.duration,
            "date": self.date.strftime('%Y-%m-%d')  # Convert date to string for JSON compatibility
        }
