import statistics

from flask.views import MethodView
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import ActivitySchema
from schemas import GetActivitySchema
from models import UserModel
from models import ActivityModel
from models import StepsModel
from models import WalkingModel
from models import RunningModel
from models import CyclingModel

blp = Blueprint("activity", "activity", description="Operations on activity")


@blp.route("/activity-list-all")
class ActivityList(MethodView):
    @jwt_required()
    @blp.arguments(GetActivitySchema)
    def post(self, activity_data):
        user_id = get_jwt_identity()

        # Fetch the activity for the user
        activity = ActivityModel.query.filter_by(user_id=user_id, date=activity_data["date"]).first()

        if not activity:
            return {"message": "Activity not found"}, 404

        # Fetch steps, walking, running, and cycling data for the given date
        steps = StepsModel.query.filter_by(steps_id=activity.steps_id, date=activity_data["date"]).all()
        walking = WalkingModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()
        running = RunningModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()
        cycling = CyclingModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()

        # Prepare the response with .to_dict() for each model instance
        activity_respond = {
            "steps": [step.to_dict() for step in steps],
            "walking": [walk.to_dict() for walk in walking],
            "running": [run.to_dict() for run in running],
            "cycling": [cycle.to_dict() for cycle in cycling]
        }

        # Return the activity with its relationships serialized
        return {"activity": activity_respond}, 200


@blp.route("/activity-list")
class ActivityList(MethodView):
    @jwt_required()
    @blp.arguments(GetActivitySchema)
    def post(self, activity_data):
        user_id = get_jwt_identity()

        # Fetch the activity for the user
        activity = ActivityModel.query.filter_by(user_id=user_id, date=activity_data["date"]).first()

        if not activity:
            user = UserModel.query.filter_by(user_id=user_id).first()
            user_name = user.name
            user_dict = {"user": [{"name": user_name}]}

            activity_respond = {
                "user": user_dict['user'],
                "steps": [],
                "walking": [],
                "running": [],
                "cycling": []
            }

            return {"activity": activity_respond}, 200

        # Fetch steps, walking, running, and cycling data for the given date
        user = UserModel.query.filter_by(user_id=user_id).first()
        steps = StepsModel.query.filter_by(steps_id=activity.steps_id, date=activity_data["date"]).all()
        walking = WalkingModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()
        running = RunningModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()
        cycling = CyclingModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()

        walking = [walk.to_dict() for walk in walking]
        running = [run.to_dict() for run in running]
        cycling = [cycle.to_dict() for cycle in cycling]

        def calculate_totals_and_averages(activity_to_calculate):
            if not activity_to_calculate:  # If the activity list is empty
                return None, None
            total_duration = sum(item['duration'] for item in activity_to_calculate)
            average_pulse = round(statistics.mean(item['average_pulse'] for item in activity_to_calculate))
            return total_duration, average_pulse

        user_name = user.name

        # Calculate for cycling, running, and walking, only if they exist
        walking_duration, walking_avg_pulse = calculate_totals_and_averages(walking)
        running_duration, running_avg_pulse = calculate_totals_and_averages(running)
        cycling_duration, cycling_avg_pulse = calculate_totals_and_averages(cycling)

        # Prepare the response
        user_dict = {"user": [{"name": user_name}]}
        steps_dict = {"steps": [step.to_dict() for step in steps]}

        # Create activity dictionaries only if data exists
        walking_dict = {"walking": [
            {"duration": walking_duration, "average_pulse": walking_avg_pulse}] if walking_duration is not None else []}
        running_dict = {"running": [
            {"duration": running_duration, "average_pulse": running_avg_pulse}] if running_duration is not None else []}
        cycling_dict = {"cycling": [
            {"duration": cycling_duration, "average_pulse": cycling_avg_pulse}] if cycling_duration is not None else []}

        # Combine the dictionaries
        activity_respond = {
            "user": user_dict['user'],
            "steps": steps_dict['steps'],
            "walking": walking_dict['walking'],
            "running": running_dict['running'],
            "cycling": cycling_dict['cycling']
        }

        return {"activity": activity_respond}, 200


@blp.route("/activity")
class ActivityAdd(MethodView):
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
