from db import db


class TrainingModel(db.Model):
    __tablename__ = 'trainings'

    training_id = db.Column(db.Integer, primary_key=True)
    walking_id = db.Column(db.Integer, db.ForeignKey('walking.walking_id'), nullable=True)
    running_id = db.Column(db.Integer, db.ForeignKey('running.running_id'), nullable=True)
    cycling_id = db.Column(db.Integer, db.ForeignKey('cycling.cycling_id'), nullable=True)

    activity = db.relationship('ActivityModel', back_populates='training')
    walking = db.relationship('WalkingModel', back_populates='training')
    running = db.relationship('RunningModel', back_populates='training')
    cycling = db.relationship('CyclingModel', back_populates='training')
