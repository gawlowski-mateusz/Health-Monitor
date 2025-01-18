#!/bin/bash

# Get the PORT from environment and set a default if not set
export FLASK_PORT="${PORT:-8080}"

# Start Gunicorn
exec gunicorn --bind "0.0.0.0:$FLASK_PORT" wsgi:app
