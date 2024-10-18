from flask.views import MethodView
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_smorest import Blueprint, abort
from sqlalchemy import desc
from sqlalchemy.exc import SQLAlchemyError
from datetime import datetime

from db import db
from schemas import RunningSchema
from models import ActivityModel
from models import TrainingModel
from models import StepsModel
from models import RunningModel

blp = Blueprint("Running", "running", description="Operations on running")


@blp.route("/running")
class RunningList(MethodView):
    @jwt_required()
    @blp.response(200, RunningSchema(many=True))
    def get(self):
        user_id = get_jwt_identity()
        activity = ActivityModel.query.filter_by(user_id=user_id, date=datetime.today().strftime('%Y-%m-%d')).first()
        training_id = activity.training_id
        return RunningModel.query.filter_by(training_id=training_id, date=datetime.today().strftime('%Y-%m-%d')).all()

    @jwt_required()
    @blp.arguments(RunningSchema)
    @blp.response(201, RunningSchema)
    def post(self, running_data):
        # Get the current logged-in user's ID from the JWT token
        user_id = get_jwt_identity()

        # Check if activity for current day exists
        if ActivityModel.query.filter_by(user_id=user_id, date=datetime.today().strftime('%Y-%m-%d')).first() is None:
            # If activity does not exist, then training also does not exist
            training = TrainingModel()

            try:
                db.session.add(training)
                db.session.commit()
            except SQLAlchemyError:
                abort(500, message="An Error has occurred.")

            # If activity does not exist, then steps also does not exist
            last_activity = ActivityModel.query.filter_by(user_id=user_id).order_by(desc(ActivityModel.date)).first()

            if last_activity is None:
                steps = StepsModel(
                    count=0,
                    goal=0,  # Default to 0 if last_steps_goal is None
                    date=datetime.today().strftime('%Y-%m-%d'),
                )
            else:
                last_steps_id = last_activity.steps_id
                last_steps = StepsModel.query.filter_by(steps_id=last_steps_id).first()
                steps = StepsModel(
                    count=0,
                    goal=last_steps.goal if last_steps.goal else 0,  # Default to 0 if last_steps_goal is None
                    date=datetime.today().strftime('%Y-%m-%d'),
                )

            try:
                db.session.add(steps)
                db.session.commit()
            except SQLAlchemyError:
                abort(500, message="An Error has occurred.")

            # Create activity fo current day
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

        # Create new running session for current day
        activity = ActivityModel.query.filter_by(user_id=user_id, date=datetime.today().strftime('%Y-%m-%d')).first()
        running = RunningModel(average_pulse=running_data["average_pulse"],
                               duration=running_data["duration"],
                               date=datetime.today().strftime('%Y-%m-%d'),
                               training_id=activity.training_id)
        try:
            db.session.add(running)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")
        return running
