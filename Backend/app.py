import os
from flask import Flask, jsonify
from flask_smorest import Api
from flask_jwt_extended import JWTManager
from datetime import timedelta
from resources.user import blp as user_blueprint
from resources.activity import blp as activity_blueprint
from resources.steps import blp as steps_blueprint
from resources.training import blp as training_blueprint
from resources.walking import blp as walking_blueprint
from resources.running import blp as running_blueprint
from resources.cycling import blp as cycling_blueprint
from flask_cors import CORS
from db import db
from blocklist import BLOCKLIST
import logging
from logging.handlers import RotatingFileHandler


def configure_logging(app):
    """Configure logging for the application"""
    if not os.path.exists('logs'):
        os.mkdir('logs')

    file_handler = RotatingFileHandler(
        'logs/health_monitor.log',
        maxBytes=10240,
        backupCount=10
    )
    file_handler.setFormatter(logging.Formatter(
        '%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]'
    ))
    file_handler.setLevel(logging.INFO)
    app.logger.addHandler(file_handler)
    app.logger.setLevel(logging.INFO)
    app.logger.info('Health Monitor startup')


def configure_jwt(app):
    """Configure JWT settings and callbacks"""
    jwt = JWTManager(app)

    @jwt.token_in_blocklist_loader
    def check_if_token_in_blacklist(jwt_header, jwt_payload):
        return jwt_payload["jti"] in BLOCKLIST

    @jwt.revoked_token_loader
    def revoked_token_callback(jwt_header, jwt_payload):
        return jsonify({
            "description": "The token has been revoked.",
            "error": "token_revoked"
        }), 401

    @jwt.needs_fresh_token_loader
    def token_not_fresh_callback(jwt_header, jwt_payload):
        return jsonify({
            "description": "The token is not fresh.",
            "error": "fresh_token_required"
        }), 401

    @jwt.expired_token_loader
    def expired_token_callback(jwt_header, jwt_payload):
        return jsonify({
            "message": "The token has expired.",
            "error": "token_expired"
        }), 401

    @jwt.invalid_token_loader
    def invalid_token_callback(error):
        return jsonify({
            "message": "Signature verification failed.",
            "error": "invalid_token"
        }), 401

    @jwt.unauthorized_loader
    def missing_token_callback(error):
        return jsonify({
            "description": "Request does not contain an access token.",
            "error": "authorization_required"
        }), 401

    return jwt


def configure_cors(app):
    """Configure CORS settings"""
    CORS(app, resources={
        r"/*": {
            "origins": os.getenv("CORS_ORIGINS", "*"),
            "methods": ["GET", "POST", "PUT", "DELETE"],
            "allow_headers": ["Content-Type", "Authorization"]
        }
    })


def configure_database(app):
    """Configure database settings"""
    app.config["SQLALCHEMY_DATABASE_URI"] = os.getenv("DATABASE_URL")
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
    app.config["SQLALCHEMY_ENGINE_OPTIONS"] = {
        "pool_pre_ping": True,
        "pool_recycle": 300,
        "pool_size": 10,
        "max_overflow": 20,
    }
    db.init_app(app)


def register_blueprints(api):
    """Register all blueprints for the application"""
    blueprints = [
        user_blueprint,
        activity_blueprint,
        steps_blueprint,
        training_blueprint,
        walking_blueprint,
        running_blueprint,
        cycling_blueprint
    ]

    for blueprint in blueprints:
        api.register_blueprint(blueprint)


def create_app(config_object=None):
    """Application factory function"""
    app = Flask(__name__)

    # Load config
    if config_object:
        app.config.from_object(config_object)

    # Basic configurations
    app.config["PROPAGATE_EXCEPTIONS"] = True
    app.config["API_TITLE"] = "Health Monitor REST API"
    app.config["API_VERSION"] = "v1"
    app.config["OPENAPI_VERSION"] = "3.0.3"
    app.config["OPENAPI_URL_PREFIX"] = "/"
    app.config["OPENAPI_SWAGGER_UI_PATH"] = "/swagger-ui"
    app.config["OPENAPI_SWAGGER_UI_URL"] = "https://cdn.jsdelivr.net/npm/swagger-ui-dist/"

    # JWT configurations
    app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET_KEY")
    app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=int(os.getenv("JWT_ACCESS_TOKEN_HOURS", 24)))
    app.config["JWT_REFRESH_TOKEN_EXPIRES"] = timedelta(days=int(os.getenv("JWT_REFRESH_TOKEN_DAYS", 30)))

    # Configure components
    configure_logging(app)
    configure_cors(app)
    configure_database(app)
    jwt = configure_jwt(app)

    # Initialize API and register blueprints
    api = Api(app)
    register_blueprints(api)

    # Database initialization
    @app.before_request
    def initialize_database():
        db.create_all()

    # Health check endpoint
    @app.route('/health')
    def health_check():
        try:
            db.session.execute('SELECT 1')
            return jsonify({'status': 'healthy', 'database': 'connected'}), 200
        except Exception as e:
            app.logger.error(f'Health check failed: {e}')
            return jsonify({'status': 'unhealthy', 'error': str(e)}), 500

    # Add security headers
    @app.after_request
    def add_security_headers(response):
        response.headers['Strict-Transport-Security'] = 'max-age=31536000; includeSubDomains'
        response.headers['X-Content-Type-Options'] = 'nosniff'
        response.headers['X-Frame-Options'] = 'SAMEORIGIN'
        response.headers['X-XSS-Protection'] = '1; mode=block'
        return response

    return app


if __name__ == '__main__':
    flask_app = create_app()
    flask_app.run(
        host='0.0.0.0',
        port=int(os.getenv('PORT', 8080))
    )
