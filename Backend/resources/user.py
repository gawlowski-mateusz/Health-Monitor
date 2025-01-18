from flask import jsonify, current_app
from flask.views import MethodView
from flask_smorest import Blueprint, abort
from passlib.hash import pbkdf2_sha256
from flask_jwt_extended import create_access_token, create_refresh_token, jwt_required, get_jwt, get_jwt_identity
from sqlalchemy.exc import SQLAlchemyError

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
            sex=user_data["sex"],
            weight=user_data.get("weight", None),
            height=user_data.get("height", None),
        )
        db.session.add(user)
        db.session.commit()

        return {"message": "User created successfully"}, 201


@blp.route("/login")
class UserLogin(MethodView):
    @blp.arguments(UserLoginSchema)
    def post(self, user_data):
        current_app.logger.info(f"Login attempt for email: {user_data['email']}")

        user = UserModel.query.filter(UserModel.email == user_data["email"]).first()

        if user and pbkdf2_sha256.verify(user_data["password"], user.password):
            user_id_str = str(user.user_id)
            access_token = create_access_token(identity=user_id_str, fresh=True)
            refresh_token = create_refresh_token(identity=user_id_str)

            current_app.logger.info(f"Login successful for user ID: {user_id_str}")

            return {"access_token": access_token, "refresh_token": refresh_token}, 200

        current_app.logger.warning(f"Failed login attempt for email: {user_data['email']}")
        abort(401, message="Invalid credentials")


@blp.route("/update-profile")
class UserUpdateProfile(MethodView):
    @jwt_required()
    @blp.arguments(UserUpdateSchema)
    def patch(self, user_data):
        user_id = get_jwt_identity()
        current_app.logger.info(f"Profile update attempt for user ID: {user_id}")
        jwt_payload = get_jwt()
        current_app.logger.debug(f"JWT payload: {jwt_payload}")

        user = UserModel.query.filter(UserModel.user_id == user_id).first()

        if not user:
            current_app.logger.error(f"User not found for ID: {user_id}")
            abort(404, message="User not found")

        # Log what's being updated
        update_fields = []
        if "password" in user_data and user_data["password"]:
            update_fields.append("password")
            user.password = pbkdf2_sha256.hash(user_data["password"])
        if "weight" in user_data and user_data["weight"] is not None:
            update_fields.append("weight")
            user.weight = user_data["weight"]
        if "height" in user_data and user_data["height"] is not None:
            update_fields.append("height")
            user.height = user_data["height"]

        current_app.logger.info(f"Updating fields for user {user_id}: {', '.join(update_fields)}")

        try:
            db.session.commit()
            current_app.logger.info(f"Profile update successful for user {user_id}")
            return {"message": "User updated successfully"}, 200
        except SQLAlchemyError as e:
            current_app.logger.error(f"Database error during profile update: {str(e)}")
            db.session.rollback()
            abort(500, message=str(e))


@blp.route("/refresh")
class TokenRefresh(MethodView):
    @jwt_required(refresh=True)
    def post(self):
        try:
            current_user = str(get_jwt_identity())  # Ensure it's a string
            current_app.logger.info(f"Refresh request for user ID: {current_user}")

            new_access_token = create_access_token(identity=current_user, fresh=False)
            new_refresh_token = create_refresh_token(identity=current_user)

            current_app.logger.info(f"New tokens created for user: {current_user}")

            return {
                "access_token": new_access_token,
                "refresh_token": new_refresh_token
            }, 200
        except Exception as e:
            current_app.logger.error(f"Error in refresh endpoint: {str(e)}", exc_info=True)
            return jsonify({
                "message": str(e),
                "error": "refresh_error"
            }), 401


@blp.route("/logout")
class UserLogout(MethodView):
    @jwt_required()
    def post(self):
        jti = get_jwt()["jti"]
        BLOCKLIST.add(jti)
        return {"message": "User successfully logged out"}, 200


@blp.route("/user/<int:user_id>")
class User(MethodView):
    @blp.response(200, UserSchema)
    def get(self, user_id):
        user = UserModel.query.get_or_404(user_id)
        return user

    @blp.response(200, UserDeleteSchema)
    def delete(self, user_id):
        user = UserModel.query.get_or_404(user_id)
        db.session.delete(user)
        db.session.commit()
        return {"message": "User deleted successfully"}, 200
