from flask.views import MethodView
from flask_smorest import Blueprint, abort
from sqlalchemy.exc import SQLAlchemyError

from db import db
from schemas import UserSchema, UserUpdateSchema
from models import UserModel

blp = Blueprint("Users", "users", description="Operations on users")


@blp.route("/user/<string:user_id>")
class User(MethodView):
    @blp.response(200, UserSchema)
    def get(self, user_id):
        raise NotImplementedError("Getting an user is not implemented.")

    def delete(self, user_id):
        raise NotImplementedError("Deleting an user is not implemented.")

    @blp.arguments(UserUpdateSchema)
    @blp.response(200, UserSchema)
    def put(self, user_data, user_id):
        raise NotImplementedError("Updating an user data is not implemented.")


@blp.route("/user")
class UserList(MethodView):
    @blp.response(200, UserSchema(many=True))
    def get(self):
        raise NotImplementedError("Listing users is not implemented.")

    @blp.arguments(UserSchema)
    @blp.response(201, UserSchema)
    def post(self, user_data):
        user = UserModel(**user_data)

        try:
            db.session.add(user)
            db.session.commit()
        except SQLAlchemyError:
            abort(500, message="An Error has occurred.")

        return user
