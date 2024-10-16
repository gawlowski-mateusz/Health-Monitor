from marshmallow import Schema, fields


# USER #
class UserSchema(Schema):
    user_id = fields.Int(dump_only=True)
    email = fields.Str(required=True)
    password = fields.Str(required=True, load_only=True)
    name = fields.Str(required=True)
    birth_date = fields.Str(required=True)
    sex = fields.Str(required=True)
    weight = fields.Float(required=False)
    height = fields.Int(required=False)


class UserLoginSchema(Schema):
    email = fields.Str(required=True)
    password = fields.Str(required=True)


class UserUpdateSchema(Schema):
    password = fields.Str(required=False)
    weight = fields.Int(required=False)
    height = fields.Int(required=False)


class UserDeleteSchema(Schema):
    user_id = fields.Int(required=True)


# ACTIVITY #
class ActivitySchema(Schema):
    activity_id = fields.Int(dump_only=True)
    user_id = fields.Int(required=True)
    steps_id = fields.Int(required=True)
    training_id = fields.Int(required=True)
    date = fields.Date(required=True)


class UpdateActivitySchema(Schema):
    user_id = fields.Int(required=False)
    steps_id = fields.Int(required=False)
    training_id = fields.Int(required=False)
    date = fields.Date(required=False)


# STEPS #
class StepsSchema(Schema):
    steps_id = fields.Int(dump_only=True)
    count = fields.Int(required=True)
    goal = fields.Int(required=True)
    date = fields.Date(required=False)


class UpdateStepsSchema(Schema):
    goal = fields.Int(required=True)


# TRAINING - WALKING, RUNNING, CYCLING
class PlainTrainingSchema(Schema):
    training_id = fields.Int(dump_only=True)


class PlainWalkingSchema(Schema):
    walking_id = fields.Int(dump_only=True)
    average_pulse = fields.Int(required=True)
    duration = fields.Int(required=True)
    date = fields.Date(required=False)


class PlainRunningSchema(Schema):
    running_id = fields.Int(dump_only=True)
    average_pulse = fields.Int(required=True)
    duration = fields.Int(required=True)
    date = fields.Date(required=False)


class PlainCyclingSchema(Schema):
    cycling_id = fields.Int(dump_only=True)
    average_pulse = fields.Int(required=True)
    duration = fields.Int(required=True)
    date = fields.Date(required=False)


class TrainingSchema(PlainTrainingSchema):
    walking_sessions = fields.List(fields.Nested(PlainWalkingSchema()), dump_only=True)
    running_sessions = fields.List(fields.Nested(PlainRunningSchema()), dump_only=True)
    cycling_sessions = fields.List(fields.Nested(PlainCyclingSchema()), dump_only=True)


class WalkingSchema(PlainWalkingSchema):
    training_id = fields.Int(load_only=True, required=False)
    training = fields.Nested(PlainTrainingSchema(), dump_only=True)


class RunningSchema(PlainRunningSchema):
    training_id = fields.Int(load_only=True, required=False)
    training = fields.Nested(PlainTrainingSchema(), dump_only=True)


class CyclingSchema(PlainCyclingSchema):
    training_id = fields.Int(load_only=True, required=False)
    training = fields.Nested(PlainTrainingSchema(), dump_only=True)
