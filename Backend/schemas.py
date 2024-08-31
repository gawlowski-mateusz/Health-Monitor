from marshmallow import Schema, fields


class UserSchema(Schema):
    user_id = fields.Int(dump_only=True)
    email = fields.Str(required=True)
    password = fields.Str(required=True)
    name = fields.Str(required=True)
    birth_date = fields.Str(required=True)
    sex = fields.Str(required=False)
    weight = fields.Int(required=False)
    height = fields.Int(required=False)


class UserUpdateSchema(Schema):
    password = fields.Str(required=False)
    weight = fields.Int(required=False)
    height = fields.Int(required=False)
