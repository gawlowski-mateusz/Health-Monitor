from db import db


class TrainingModel(db.Model):
    __tablename__ = 'training'

    training_id = db.Column(db.Integer, primary_key=True)

    activity = db.relationship('ActivityModel', back_populates='training', lazy='dynamic')
    walking = db.relationship('WalkingModel', back_populates='training', lazy="dynamic")
    running = db.relationship('RunningModel', back_populates='training', lazy="dynamic")
    cycling = db.relationship('CyclingModel', back_populates='training', lazy="dynamic")
