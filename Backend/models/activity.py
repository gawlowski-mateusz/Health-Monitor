from db import db


class ActivityModel(db.Model):
    __tablename__ = 'activities'

    activity_id = db.Column(db.Integer, nullable=False, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    training_id = db.Column(db.Integer, db.ForeignKey('training.training_id'), nullable=False)
    steps_id = db.Column(db.Integer, db.ForeignKey('steps.steps_id'), nullable=False)
    date = db.Column(db.Date, nullable=False)

    user = db.relationship('UserModel', back_populates='activity')
    training = db.relationship('TrainingModel', back_populates='activity')
    steps = db.relationship('StepsModel', back_populates='activity')

    def to_dict(self):
        return {
            "activity_id": self.activity_id,
            "user_id": self.user_id,
            "training_id": self.training_id,
            "steps_id": self.steps_id,
            "date": self.date.strftime('%Y-%m-%d'),  # Convert date to string
            # Optionally include relationships
            "user": self.user.to_dict(),  # Uncomment if UserModel has a to_dict method
            "training": self.training.to_dict(),  # Uncomment if TrainingModel has a to_dict method
            "steps": self.steps.to_dict()  # Uncomment if StepsModel has a to_dict method
        }

