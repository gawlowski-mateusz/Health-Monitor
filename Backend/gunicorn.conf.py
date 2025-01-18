import multiprocessing
import os

# Binding
bind = f"0.0.0.0:{os.getenv('PORT', '8080')}"

# Worker processes
workers = int(os.getenv('GUNICORN_WORKERS', multiprocessing.cpu_count() * 2 + 1))
worker_class = 'gevent'
worker_connections = 1000
max_requests = 1000
max_requests_jitter = 50
timeout = 30
keepalive = 5

# Logging
accesslog = '-'
errorlog = '-'
loglevel = os.getenv('GUNICORN_LOG_LEVEL', 'info')

# Process naming
proc_name = 'health_monitor'

# Server mechanics
daemon = False
pidfile = None
umask = 0
user = None
group = None