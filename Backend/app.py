import os

from flask import Flask, jsonify
from flask_smorest import Api
from flask_jwt_extended import JWTManager
from resources.user import blp as user_blueprint
from resources.activity import blp as activity_blueprint
from resources.steps import blp as steps_blueprint
from resources.training import blp as training_blueprint
from resources.walking import blp as walking_blueprint
from resources.running import blp as running_blueprint
from resources.cycling import blp as cycling_blueprint

from db import db
from blocklist import BLOCKLIST


def create_app(db_url=None):
    app = Flask(__name__)

    app.config["PROPAGATE_EXCEPTIONS"] = True
    app.config["API_TITLE"] = "Health Monitor REST API"
    app.config["API_VERSION"] = "v1"
    app.config["OPENAPI_VERSION"] = "3.0.3"
    app.config["OPENAPI_URL_PREFIX"] = "/"
    app.config["OPENAPI_SWAGGER_UI_PATH"] = "/swagger-ui"
    app.config["OPENAPI_SWAGGER_UI_URL"] = "https://cdn.jsdelivr.net/npm/swagger-ui-dist/"
    app.config["SQLALCHEMY_DATABASE_URI"] = db_url or os.getenv("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/postgres")
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

    db.init_app(app)
    api = Api(app)
    # app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET_KEY") or secrets.SystemRandom().getrandbits(128)
    app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET_KEY") or "120743901920933760412580320966469808258"
    jwt = JWTManager(app)

    # @jwt.additional_claims_loader
    # def add_claims_to_jwt(identity):
    #     # TODO: Read from a config file instead of hard-coding
    #     if identity == 1:
    #         return {"is_admin": True}
    #     return {"is_admin": False}

    @jwt.token_in_blocklist_loader
    def check_if_token_in_blacklist(jwt_header, jwt_payload):
        return jwt_payload["jti"] in BLOCKLIST

    @jwt.revoked_token_loader
    def revoked_token_callback(jwt_header, jwt_payload):
        return jsonify({"description": "The token has been revoked.", "error": "token_revoked"}), 401

    @jwt.needs_fresh_token_loader
    def token_not_fresh_callback(jwt_header, jwt_payload):
        return jsonify({"description": "The token is not fresh.", "error": "fresh_token_required"}), 401

    @jwt.expired_token_loader
    def expired_token_callback(jwt_header, jwt_payload):
        return jsonify({"message": "The token has expired.", "error": "token_expired"}), 401

    @jwt.invalid_token_loader
    def invalid_token_callback(error):
        return jsonify({"message": "Signature verification failed.", "error": "invalid_token"}), 401

    @jwt.unauthorized_loader
    def missing_token_callback(error):
        return jsonify(
            {"description": "Request does not contain an access token.", "error": "authorization_required"}), 401

    @app.before_request
    def create_tables():
        db.create_all()

    api.register_blueprint(user_blueprint)
    api.register_blueprint(activity_blueprint)
    api.register_blueprint(steps_blueprint)
    api.register_blueprint(training_blueprint)
    api.register_blueprint(walking_blueprint)
    api.register_blueprint(running_blueprint)
    api.register_blueprint(cycling_blueprint)

    @app.route('/')
    def hello_flask():  # put application's code here
        return 'Flask run!'

    @app.route('/test')
    def test():  # put application's code here
        return 'Hello World!'

    @app.route('/api/auth/sign-up')
    def sing_up():  # put application's code here
        return 'Sign up successfully!'

    @app.route('/api/auth/login')
    def login():  # put application's code here
        return 'Login successfully!'

    return app
