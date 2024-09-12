from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import WalkingSchema
from models import WalkingModel

blp = Blueprint("Walking", "walking", description="Operations on Walking")


@blp.route("/walking")
class WalkingList(MethodView):
    @blp.response(200, WalkingSchema(many=True))
    def get(self):
        raise NotImplementedError("Listing walking is not implemented.")

    @blp.arguments(WalkingSchema)
    @blp.response(201, WalkingSchema)
    def post(self, walking_data):
        walking = WalkingModel(**walking_data)

        try:
            db.session.add(walking)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return walking
