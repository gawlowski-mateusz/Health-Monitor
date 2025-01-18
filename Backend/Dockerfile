FROM python:3.9

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt
RUN pip install gunicorn

COPY . .
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

ENV PYTHONPATH=/app

CMD ["./entrypoint.sh"]