from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import TrainingSchema
from models import TrainingModel

blp = Blueprint("Training", "training", description="Operations on training")


@blp.route("/training")
class TrainingList(MethodView):
    @blp.response(200, TrainingSchema(many=True))
    def get(self):
        return TrainingModel.query.all()

    @blp.arguments(TrainingSchema)
    @blp.response(201, TrainingSchema)
    def post(self, training_data):
        training = TrainingModel(**training_data)

        try:
            db.session.add(training)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return training
