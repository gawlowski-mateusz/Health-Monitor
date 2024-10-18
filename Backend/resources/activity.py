import statistics

from flask.views import MethodView
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import ActivitySchema
from schemas import GetActivitySchema
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
            return {"message": "Activity not found"}, 404

        # Fetch steps, walking, running, and cycling data for the given date
        steps = StepsModel.query.filter_by(steps_id=activity.steps_id, date=activity_data["date"]).all()
        walking = WalkingModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()
        running = RunningModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()
        cycling = CyclingModel.query.filter_by(training_id=activity.training_id, date=activity_data["date"]).all()

        walking = [walk.to_dict() for walk in walking]
        running = [run.to_dict() for run in running]
        cycling = [cycle.to_dict() for cycle in cycling]

        # Function to sum durations and calculate average pulse
        def calculate_totals_and_averages(activity_to_calculate):
            total_duration = sum(item['duration'] for item in activity_to_calculate)
            average_pulse = statistics.mean(item['average_pulse'] for item in activity_to_calculate)
            return total_duration, average_pulse

        # Calculate for cycling, running, and walking
        walking_duration, walking_avg_pulse = calculate_totals_and_averages(walking)
        running_duration, running_avg_pulse = calculate_totals_and_averages(running)
        cycling_duration, cycling_avg_pulse = calculate_totals_and_averages(cycling)

        # Prepare the response
        steps_dict = {"steps": [step.to_dict() for step in steps]}
        walking_dict = {"walking": [{"duration": walking_duration, "average_pulse": walking_avg_pulse}]}
        running_dict = {"running": [{"duration": running_duration, "average_pulse": running_avg_pulse}]}
        cycling_dict = {"cycling": [{"duration": cycling_duration, "average_pulse": cycling_avg_pulse}]}

        # Combine the dictionaries correctly
        activity_respond = {
            "steps": steps_dict['steps'],
            "walking": walking_dict['walking'],
            "running": running_dict['running'],
            "cycling": cycling_dict['cycling']
        }

        # Return the activity with its relationships serialized
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
