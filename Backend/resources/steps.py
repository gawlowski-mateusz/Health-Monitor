import jwt
from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError, IntegrityError
from flask_jwt_extended import jwt_required

from db import db
from schemas import StepsSchema, UpdateStepsSchema
from models import StepsModel

blp = Blueprint("Steps", "steps", description="Operations on steps")


# @blp.route("/steps/<string:steps_id>")
# class Steps(MethodView):
#     @blp.response(200, StepsSchema)
#     def get(self, steps_id):
#         raise NotImplementedError("Getting a steps is not implemented.")
#
#     def delete(self, steps_id):
#         raise NotImplementedError("Deleting a steps is not implemented.")
#
#     @blp.arguments(StepsUpdateSchema)
#     @blp.response(200, StepsSchema)
#     def put(self, steps_data, steps_id):
#         raise NotImplementedError("Updating a steps data is not implemented.")


@blp.route("/steps")
class StepsList(MethodView):
    @blp.response(200, StepsSchema(many=True))
    def get(self):
        raise NotImplementedError("Listing steps is not implemented.")

    @jwt_required()
    @blp.arguments(StepsSchema)
    @blp.response(201, StepsSchema)
    def post(self, steps_data):
        steps = StepsModel(**steps_data)

        try:
            db.session.add(steps)
            db.session.commit()
        except IntegrityError:
            abort(400, message="Integrity Error")
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return steps
