from db import db


class ActivityModel(db.Model):
    __tablename__ = 'activities'

    activity_id = db.Column(db.Integer, nullable=False, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    training_id = db.Column(db.Integer, db.ForeignKey('trainings.training_id'), nullable=False)
    steps_id = db.Column(db.Integer, db.ForeignKey('steps.steps_id'), nullable=False)
    date = db.Column(db.Date, nullable=False, unique=True)

    user = db.relationship('UserModel', back_populates='activities')
    training = db.relationship('TrainingModel', back_populates='activities')
    steps = db.relationship('StepModel', back_populates='activities')
