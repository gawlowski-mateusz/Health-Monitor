from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import CyclingSchema
from models import CyclingModel

blp = Blueprint("cycling", "cycling", description="Operations on cycling")


@blp.route("/cycling")
class CyclingList(MethodView):
    @blp.response(200, CyclingSchema(many=True))
    def get(self):
        raise NotImplementedError("Listing cycling is not implemented.")

    @blp.arguments(CyclingSchema)
    @blp.response(201, CyclingSchema)
    def post(self, cycling_data):
        cycling = CyclingModel(**cycling_data)

        try:
            db.session.add(cycling)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return cycling
