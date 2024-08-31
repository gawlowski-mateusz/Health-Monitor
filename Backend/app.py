import os
import psycopg2
from dotenv import load_dotenv
from flask import Flask
from flask_smorest import Api
from resources.user import blp as user_blueprint

# load_dotenv()
app = Flask(__name__)
# url = os.getenv('DATABASE_URL')
# connection = psycopg2.connect(url)

app.config["PROPAGATE_EXCEPTIONS"] = True
app.config["API_TITLE"] = "Health Monitor REST API"
app.config["API_VERSION"] = "v1"
app.config["OPENAPI_VERSION"] = "3.0.3"
app.config["OPENAPI_URL_PREFIX"] = "/"
app.config["OPENAPI_SWAGGER_UI_PATH"] = "/swagger-ui"
app.config["OPENAPI_SWAGGER_UI_URL"] = "https://cdn.jsdelivr.net/npm/swagger-ui-dist/"

api = Api(app)
api.register_blueprint(user_blueprint)


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
