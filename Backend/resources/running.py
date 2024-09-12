from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import RunningSchema
from models import RunningModel

blp = Blueprint("Running", "running", description="Operations on running")


@blp.route("/running")
class RunningList(MethodView):
    @blp.response(200, RunningSchema(many=True))
    def get(self):
        raise NotImplementedError("Listing running is not implemented.")

    @blp.arguments(RunningSchema)
    @blp.response(201, RunningSchema)
    def post(self, running_data):
        running = RunningModel(**running_data)

        try:
            db.session.add(running)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return running
