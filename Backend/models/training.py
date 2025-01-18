from db import db


class TrainingModel(db.Model):
    __tablename__ = 'training'

    training_id = db.Column(db.Integer, primary_key=True)

    activity = db.relationship('ActivityModel', back_populates='training', lazy='dynamic')
    walking = db.relationship('WalkingModel', back_populates='training', lazy="dynamic")
    running = db.relationship('RunningModel', back_populates='training', lazy="dynamic")
    cycling = db.relationship('CyclingModel', back_populates='training', lazy="dynamic")

    def to_dict(self, include_relationships=True):
        training_dict = {
            "training_id": self.training_id
        }

        if include_relationships:
            # training_dict["activity"] = [act.to_dict() for act in self.activity]  # Assuming ActivityModel to_dict()
            training_dict["walking"] = [walk.to_dict() for walk in self.walking]  # Assuming WalkingModel has to_dict()
            training_dict["running"] = [run.to_dict() for run in self.running]  # Assuming RunningModel has to_dict()
            training_dict["cycling"] = [cycle.to_dict() for cycle in self.cycling]  # Assuming CyclingModel to_dict()

        return training_dict
