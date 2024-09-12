from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import ActivitySchema
from models import ActivityModel

blp = Blueprint("activity", "activity", description="Operations on activity")


@blp.route("/activity")
class ActivityList(MethodView):
    @blp.response(200, ActivitySchema(many=True))
    def get(self):
        raise NotImplementedError("Listing activity is not implemented.")

    @blp.arguments(ActivitySchema)
    @blp.response(201, ActivitySchema)
    def post(self, activity_data):
        activity = ActivityModel(**activity_data)

        try:
            db.session.add(activity)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return activity
