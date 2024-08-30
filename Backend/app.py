import os
import psycopg2
from dotenv import load_dotenv
from flask import Flask

# load_dotenv()
app = Flask(__name__)
# url = os.getenv('DATABASE_URL')
# connection = psycopg2.connect(url)


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
