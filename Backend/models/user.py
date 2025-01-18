from db import db


class UserModel(db.Model):
    __tablename__ = 'users'

    user_id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(50), unique=True, nullable=False)
    password = db.Column(db.String(1024), nullable=False)
    name = db.Column(db.String(50), nullable=False)
    birth_date = db.Column(db.Date, nullable=False)
    sex = db.Column(db.String(20), nullable=False)
    weight = db.Column(db.Float(precision=1), nullable=True)
    height = db.Column(db.Integer, nullable=True)

    activity = db.relationship('ActivityModel', back_populates="user", lazy="dynamic")

    def to_dict(self):
        return {
            "user_id": self.user_id,
            "email": self.email,  # Only include fields you want in the JSON response
            "name": self.name,
            "birth_date": self.birth_date.strftime('%Y-%m-%d'),  # Convert date to string
            "sex": self.sex,
            "weight": self.weight,
            "height": self.height,
            # Optionally, you can include the activities if needed
            # "activities": [activity.to_dict() for activity in self.activity]  # If ActivityModel has a to_dict()
        }
