from flask.views import MethodView
from flask_smorest import Blueprint, abort
from passlib.hash import pbkdf2_sha256
from flask_jwt_extended import create_access_token,create_refresh_token, jwt_required, get_jwt, get_jwt_identity

from db import db
from blocklist import BLOCKLIST
from schemas import UserSchema, UserUpdateSchema, UserDeleteSchema, UserLoginSchema
from models import UserModel

blp = Blueprint("Users", "users", description="Operations on users")


@blp.route("/register")
class UserRegister(MethodView):
    @blp.arguments(UserSchema)
    def post(self, user_data):
        if UserModel.query.filter(UserModel.email == user_data["email"]).first():
            abort(409, message="User with that email address already exists")

        user = UserModel(
            email=user_data["email"],
            password=pbkdf2_sha256.hash(user_data["password"]),
            name=user_data["name"],
            birth_date=user_data["birth_date"],
            sex=user_data["sex"]
        )
        db.session.add(user)
        db.session.commit()

        return {"message": "User created successfully"}, 201


@blp.route("/login")
class UserLogin(MethodView):
    @blp.arguments(UserLoginSchema)
    def post(self, user_data):
        user = UserModel.query.filter(UserModel.email == user_data["email"]).first()

        if user and pbkdf2_sha256.verify(user_data["password"], user.password):
            access_token = create_access_token(identity=user.user_id, fresh=True)
            refresh_token = create_refresh_token(identity=user.user_id)
            return {"access_token": access_token, "refresh_token": refresh_token}, 200

        abort(401, message="Invalid credentials")


@blp.route("/refresh")
class TokenRefresh(MethodView):
    @jwt_required(refresh=True)
    def post(self):
        current_user = get_jwt_identity()
        new_access_token = create_access_token(identity=current_user, fresh=False)
        return {"access_token": new_access_token}, 200


@blp.route("/logout")
class UserLogout(MethodView):
    @jwt_required()
    def post(self):
        jti = get_jwt()["jti"]
        BLOCKLIST.add(jti)
        return {"message": "User successfully logged out"}, 200


@blp.route("/user/<int:user_id>")
class User(MethodView):
    @blp.response(200, UserDeleteSchema)
    def get(self, user_id):
        user = UserModel.query.get_or_404(user_id)
        return user

    @blp.response(200, UserDeleteSchema)
    def delete(self, user_id):
        user = UserModel.query.get_or_404(user_id)
        db.session.delete(user)
        db.session.commit()
        return {"message": "User deleted successfully"}, 200

    @blp.arguments(UserUpdateSchema)
    @blp.response(200, UserUpdateSchema)
    def put(self, user_data, user_id):
        raise NotImplementedError("Updating user data is not implemented.")
