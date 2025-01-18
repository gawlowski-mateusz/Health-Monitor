#!/bin/bash

export FLASK_PORT="${PORT:-8080}"

# number of workers (2 x CPU cores + 1)
WORKERS=$((2 * $(nproc) + 1))
if [ "$WORKERS" -gt 4 ]; then
    WORKERS=4
fi

exec gunicorn --workers=$WORKERS \
    --worker-class=gevent \
    --bind "0.0.0.0:$FLASK_PORT" \
    --max-requests=1000 \
    --max-requests-jitter=50 \
    --timeout=30 \
    --keep-alive=5 \
    wsgi:app