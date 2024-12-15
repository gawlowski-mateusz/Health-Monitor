from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError, IntegrityError
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime

from db import db
from schemas import StepsSchema, UpdateStepsSchema
from models import StepsModel, TrainingModel
from models import ActivityModel

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


@blp.route("/steps/goal")
class StepsGoal(MethodView):
    # @jwt_required()
    @blp.arguments(UpdateStepsSchema)
    @blp.response(201, StepsSchema)
    def post(self, steps_data):
        steps = StepsModel(
            count=0,
            goal=steps_data["goal"],
            date=datetime.today().strftime('%Y-%m-%d'),
        )

        try:
            db.session.add(steps)
            db.session.commit()
        except IntegrityError:
            abort(400, message="Integrity Error")
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return steps

    @jwt_required()
    @blp.arguments(UpdateStepsSchema)
    @blp.response(201, UpdateStepsSchema)
    def patch(self, steps_data):
        # Get the current logged-in user's ID from the JWT token
        user_id = get_jwt_identity()
        activity = ActivityModel.query.filter_by(user_id=user_id, date=datetime.today().strftime('%Y-%m-%d')).first()

        # Check if activity for current day exists
        if activity is None:
            # If activity does not exist, then training also does not exist
            training = TrainingModel()

            try:
                db.session.add(training)
                db.session.commit()
            except SQLAlchemyError:
                abort(500, message="An Error has occurred.")

            # If activity does not exist, then steps also does not exist
            steps = StepsModel(
                count=steps_data.get("count", 0),  # Default to 0 if count is not provided
                goal=steps_data.get("goal", 0),  # Default to 0 if goal is not provided
                date=datetime.today().strftime('%Y-%m-%d'),
            )

            try:
                db.session.add(steps)
                db.session.commit()
            except SQLAlchemyError:
                abort(500, message="An Error has occurred.")

            # Create activity for current day
            activity = ActivityModel(
                user_id=user_id,
                steps_id=steps.steps_id,
                training_id=training.training_id,
                date=datetime.today().strftime('%Y-%m-%d'),
            )
            try:
                db.session.add(activity)
                db.session.commit()
            except SQLAlchemyError:
                abort(500, message="An Error has occurred.")
        else:
            steps = StepsModel.query.filter_by(steps_id=activity.steps_id).first()

            # Only update fields that are present in the request
            if "goal" in steps_data:
                steps.goal = steps_data["goal"]
            if "count" in steps_data:
                steps.count = steps_data["count"]

            try:
                db.session.commit()
            except SQLAlchemyError:
                abort(500, message="An Error has occurred.")

        return {"message": "Steps updated successfully"}, 201
