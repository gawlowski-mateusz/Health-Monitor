import os
from flask import Flask
from flask_smorest import Api
from resources.user import blp as user_blueprint
from resources.activity import blp as activity_blueprint
from resources.steps import blp as steps_blueprint
from resources.training import blp as training_blueprint
from resources.walking import blp as walking_blueprint
from resources.running import blp as running_blueprint
from resources.cycling import blp as cycling_blueprint
from db import db
import models


def create_app(db_url=None):
    app = Flask(__name__)

    app.config["PROPAGATE_EXCEPTIONS"] = True
    app.config["API_TITLE"] = "Health Monitor REST API"
    app.config["API_VERSION"] = "v1"
    app.config["OPENAPI_VERSION"] = "3.0.3"
    app.config["OPENAPI_URL_PREFIX"] = "/"
    app.config["OPENAPI_SWAGGER_UI_PATH"] = "/swagger-ui"
    app.config["OPENAPI_SWAGGER_UI_URL"] = "https://cdn.jsdelivr.net/npm/swagger-ui-dist/"
    app.config["SQLALCHEMY_DATABASE_URI"] = (db_url or os.getenv("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/postgres"))
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

    db.init_app(app)
    api = Api(app)

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
