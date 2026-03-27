# Health-Monitor

A comprehensive health and fitness tracking application with a robust REST API backend and an Android mobile frontend, integrated with smartwatch support via PineTime.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [PineTime Configuration](#pinetime-configuration)
- [API Documentation](#api-documentation)
- [Database Models](#database-models)
- [Usage](#usage)
- [Docker Deployment](#docker-deployment)
- [Configuration](#configuration)
- [Logging](#logging)
- [Contributing](#contributing)

## 🎯 Overview

Health-Monitor is a full-stack health tracking platform that allows users to:
- Track multiple types of physical activities (running, cycling, walking)
- Monitor daily steps
- Track training sessions
- Manage user profiles with authentication
- Sync data with PineTime smartwatch devices

The project follows a modern architecture with a Flask REST API backend using PostgreSQL and a native Android frontend written in Kotlin.

## ✨ Features

### Backend
- 🔐 JWT-based authentication with token management
- 📊 Multiple activity tracking endpoints (Running, Cycling, Walking, Steps, Training)
- 👤 User management and profile management
- 🗄️ PostgreSQL database with SQLAlchemy ORM
- 📝 Comprehensive logging system
- 🚀 Production-ready with Gunicorn and Gevent
- 🐳 Docker containerization support
- 📜 CORS support for cross-origin requests

### Frontend
- 📱 Native Android application built with Kotlin
- 🔑 Secure login and registration
- 📊 Activity view screens for different exercise types
- ⌚ Bluetooth connectivity for smartwatch data
- 🖼️ User profile management
- 📋 Session management (running, cycling, walking)
- 🎨 Material Design UI

### PineTime Integration
- ⌚ Smartwatch connection management
- 📲 Real-time notifications
- 🔋 Battery level monitoring
- ⏰ Time synchronization

## 📁 Project Structure

```
Health-Monitor/
├── Backend/                          # Flask REST API
│   ├── models/                       # SQLAlchemy data models
│   │   ├── user.py                   # User model
│   │   ├── activity.py               # Base activity model
│   │   ├── running.py                # Running sessions
│   │   ├── cycling.py                # Cycling sessions
│   │   ├── walking.py                # Walking sessions
│   │   ├── steps.py                  # Daily steps tracking
│   │   └── training.py               # Training sessions
│   ├── resources/                    # API endpoints (Flask-RESTX)
│   │   ├── user.py                   # User endpoints
│   │   ├── activity.py               # Activity endpoints
│   │   ├── running.py                # Running endpoints
│   │   ├── cycling.py                # Cycling endpoints
│   │   ├── walking.py                # Walking endpoints
│   │   ├── steps.py                  # Steps endpoints
│   │   └── training.py               # Training endpoints
│   ├── app.py                        # Flask application factory
│   ├── db.py                         # Database initialization
│   ├── blocklist.py                  # JWT token blocklist
│   ├── schemas.py                    # Marshmallow schemas
│   ├── requirements.txt              # Python dependencies
│   ├── Dockerfile                    # Docker configuration
│   ├── docker-compose.yml            # Docker compose setup
│   ├── gunicorn.conf.py             # Gunicorn configuration
│   └── wsgi.py                       # WSGI entry point
│
├── Frontend/                         # Android Kotlin Application
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/mateusz/frontend/
│   │   │   │   │   ├── MainActivity.kt              # Main activity
│   │   │   │   │   ├── LoginScreen.kt              # Login screen
│   │   │   │   │   ├── RegisterScreen.kt           # Registration screen
│   │   │   │   │   ├── HomeScreen.kt               # Home dashboard
│   │   │   │   │   ├── ProfileViewScreen.kt        # User profile view
│   │   │   │   │   ├── EditProfileScreen.kt        # Profile editing
│   │   │   │   │   ├── OverviewScreen.kt           # Activity overview
│   │   │   │   │   ├── CyclingSessionsScreen.kt    # Cycling sessions
│   │   │   │   │   ├── RunningSessionsScreen.kt    # Running sessions
│   │   │   │   │   ├── WalkingSessionsScreen.kt    # Walking sessions
│   │   │   │   │   ├── NewCyclingSessionScreen.kt  # New cycling session
│   │   │   │   │   ├── NewRunningSessionScreen.kt  # New running session
│   │   │   │   │   ├── NewWalkingSessionScreen.kt  # New walking session
│   │   │   │   │   ├── EditStepsScreen.kt          # Steps editor
│   │   │   │   │   ├── BackgroundService.kt        # Background sync service
│   │   │   │   │   ├── BluetoothMeasurementsManager.kt  # Bluetooth handling
│   │   │   │   │   ├── TokenManager.kt              # JWT token management
│   │   │   │   │   ├── NetworkConfig.kt             # API configuration
│   │   │   │   │   ├── TestAPI.kt                   # API testing utilities
│   │   │   │   │   └── ui/                          # UI components
│   │   │   │   ├── res/                            # Resources
│   │   │   │   │   ├── drawable/                   # Icons and drawables
│   │   │   │   │   ├── values/                     # Colors, strings, themes
│   │   │   │   │   ├── xml/                        # Network security config
│   │   │   │   │   └── raw/                        # Raw resources (certificates)
│   │   │   │   └── AndroidManifest.xml
│   │   │   ├── androidTest/                        # Instrumented tests
│   │   │   └── test/                               # Unit tests
│   │   ├── build.gradle.kts                        # App-level build config
│   │   └── proguard-rules.pro                      # ProGuard rules
│   ├── build.gradle.kts                            # Project-level build config
│   ├── gradle.properties                           # Gradle properties
│   ├── settings.gradle.kts                         # Gradle settings
│   ├── gradlew / gradlew.bat                       # Gradle wrapper
│   └── gradle/
│       ├── libs.versions.toml                      # Dependency versions
│       └── wrapper/                                # Gradle wrapper files
│
├── Pine-Time-Config/                 # PineTime Smartwatch Configuration
│   ├── pinetime_connect.py           # PineTime connection module
│   ├── run_pinetime.py               # PineTime runtime script
│   └── __pycache__/
│
├── Diagrams/                         # Architecture and database diagrams
│   └── data-base-diagram.drawio      # Database schema diagram
│
└── README.md                         # This file

```

## 🛠️ Tech Stack

### Backend
| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Flask | 3.0.3 |
| API | Flask-RESTX (Flask-Smorest) | Latest |
| Authentication | Flask-JWT-Extended | 4.6.0 |
| Database | PostgreSQL + SQLAlchemy | 2.0.37 |
| ORM | SQLAlchemy | 2.0.37 |
| Serialization | Marshmallow | 3.22.0 |
| Security | Passlib + PyOpenSSL | Latest |
| Web Server | Gunicorn | 20.1.0+ |
| ASGI Worker | Gevent | 22.10.2+ |
| CORS | Flask-CORS | 5.0.0 |

### Frontend
| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | Latest |
| Android SDK | Android 26+ (minSdk) | API 34 (compileSdk) |
| Build System | Gradle | 8.x |
| Target | Android API 34 |

### Infrastructure
| Component | Technology |
|-----------|-----------|
| Containerization | Docker |
| Orchestration | Docker Compose |
| Database | PostgreSQL (in container) |

## 📋 Prerequisites

### Backend
- Python 3.9+
- PostgreSQL 12+
- pip (Python package manager)

### Frontend
- Android Studio Arctic Fox or newer
- Java Development Kit (JDK) 11+
- Android SDK 34
- Gradle 8.x

### PineTime
- Python 3.9+
- Bluetooth-enabled device
- PineTime smartwatch

## 🚀 Installation & Setup

### Backend Setup

1. **Navigate to Backend directory:**
```bash
cd Backend
```

2. **Create and activate virtual environment:**
```bash
python -m venv venv
# On Windows
venv\Scripts\activate
# On macOS/Linux
source venv/bin/activate
```

3. **Install dependencies:**
```bash
pip install -r requirements.txt
```

4. **Create `.env` file in Backend directory:**
```env
FLASK_ENV=development
FLASK_DEBUG=1
DATABASE_URL=postgresql://user:password@localhost:5432/health_monitor
JWT_SECRET_KEY=your-secret-key-here
JWT_ACCESS_TOKEN_EXPIRES=3600
```

5. **Initialize database:**
```bash
flask db upgrade
```

6. **Run development server:**
```bash
flask run
```

The API will be available at `http://localhost:5000`

### Frontend Setup

1. **Open Frontend directory in Android Studio:**
```bash
cd Frontend
```

2. **Set SDK paths** (if not already configured):
   - Go to File → Project Structure
   - Set Android SDK location

3. **Configure API endpoint** in [NetworkConfig.kt](Frontend/app/src/main/java/com/mateusz/frontend/NetworkConfig.kt):
```kotlin
const val BASE_URL = "http://your-backend-url:5000"
```

4. **Build the project:**
```bash
./gradlew build
```

5. **Run on emulator or device:**
```bash
./gradlew installDebug
```

### PineTime Configuration

1. **Navigate to Pine-Time-Config directory:**
```bash
cd Pine-Time-Config
```

2. **Install dependencies:**
```bash
pip install -r ../Backend/requirements.txt  # or install BLE libraries
```

3. **Configure device connection** in [pinetime_connect.py](Pine-Time-Config/pinetime_connect.py)

4. **Run PineTime sync:**
```bash
python run_pinetime.py
```

## 📡 API Documentation

### Authentication Endpoints

**Login**
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Register**
```
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "username",
  "password": "password123"
}
```

**Logout** (requires JWT token)
```
POST /api/auth/logout
Authorization: Bearer <access_token>
```

### Activity Endpoints

All activity endpoints require JWT authentication.

**Running Sessions**
```
GET    /api/running              # List running sessions
POST   /api/running              # Create new running session
GET    /api/running/<id>         # Get running session details
PUT    /api/running/<id>         # Update running session
DELETE /api/running/<id>         # Delete running session
```

**Cycling Sessions**
```
GET    /api/cycling              # List cycling sessions
POST   /api/cycling              # Create new cycling session
GET    /api/cycling/<id>         # Get cycling session details
PUT    /api/cycling/<id>         # Update cycling session
DELETE /api/cycling/<id>         # Delete cycling session
```

**Walking Sessions**
```
GET    /api/walking              # List walking sessions
POST   /api/walking              # Create new walking session
GET    /api/walking/<id>         # Get walking session details
PUT    /api/walking/<id>         # Update walking session
DELETE /api/walking/<id>         # Delete walking session
```

**Steps Tracking**
```
GET    /api/steps                # Get daily steps
POST   /api/steps                # Record steps
PUT    /api/steps/<id>           # Update steps
DELETE /api/steps/<id>           # Delete steps record
```

**Training Sessions**
```
GET    /api/training             # List training sessions
POST   /api/training             # Create new training session
GET    /api/training/<id>        # Get training session details
PUT    /api/training/<id>        # Update training session
DELETE /api/training/<id>        # Delete training session
```

### User Endpoints

**Get Profile**
```
GET /api/user/profile
Authorization: Bearer <access_token>
```

**Update Profile**
```
PUT /api/user/profile
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "username": "new_username",
  "email": "newemail@example.com"
}
```

## 💾 Database Models

### User
- `id` (UUID, Primary Key)
- `email` (String, Unique)
- `username` (String, Unique)
- `password_hash` (String)
- `created_at` (DateTime)
- `updated_at` (DateTime)

### Activity (Base Class)
- `id` (UUID, Primary Key)
- `user_id` (Foreign Key to User)
- `date` (Date)
- `duration` (Integer - minutes)
- `distance` (Float - kilometers)
- `calories` (Float)
- `notes` (String, Optional)
- `created_at` (DateTime)

### Running (extends Activity)
- Inherited from Activity
- `average_pace` (Float)
- `max_pace` (Float)

### Cycling (extends Activity)
- Inherited from Activity
- `average_speed` (Float)
- `max_speed` (Float)
- `elevation_gain` (Float)

### Walking (extends Activity)
- Inherited from Activity
- `average_pace` (Float)
- `steps_count` (Integer)

### Steps
- `id` (UUID, Primary Key)
- `user_id` (Foreign Key to User)
- `date` (Date)
- `step_count` (Integer)
- `created_at` (DateTime)

### Training
- `id` (UUID, Primary Key)
- `user_id` (Foreign Key to User)
- `name` (String)
- `description` (String, Optional)
- `activities` (List of Activity References)
- `created_at` (DateTime)

## 💻 Usage

### Mobile Application

1. **Launch the app** on your Android device
2. **Register or Login** with your credentials
3. **View Dashboard** with activity overview
4. **Start tracking:**
   - Tap on activity type (Running, Cycling, Walking)
   - Enter session details
   - Save the session
5. **View Sessions:**
   - Navigate to specific activity screens
   - View session history
6. **Manage Profile:**
   - Edit personal information in profile settings
   - View statistics

### Backend API Usage with cURL

**Register User:**
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "user",
    "password": "password123"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Create Running Session:**
```bash
curl -X POST http://localhost:5000/api/running \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "date": "2024-03-27",
    "duration": 30,
    "distance": 5.5,
    "calories": 350,
    "average_pace": 5.5,
    "max_pace": 6.2
  }'
```

## 🐳 Docker Deployment

### Using Docker Compose

1. **Navigate to Backend directory:**
```bash
cd Backend
```

2. **Configure `.env` file** for production:
```env
FLASK_ENV=production
DATABASE_URL=postgresql://postgres:password@db:5432/health_monitor
JWT_SECRET_KEY=your-production-secret-key
```

3. **Start services:**
```bash
docker-compose up -d
```

4. **Check logs:**
```bash
docker-compose logs -f app
```

5. **Shutdown services:**
```bash
docker-compose down
```

### Manual Docker Build

```bash
# Build image
docker build -t health-monitor-backend .

# Run container
docker run -p 5000:5000 \
  -e DATABASE_URL=postgresql://user:pass@db:5432/health_monitor \
  -e JWT_SECRET_KEY=secret-key \
  health-monitor-backend
```

## ⚙️ Configuration

### Backend Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `FLASK_ENV` | Environment mode (development/production) | development |
| `FLASK_DEBUG` | Enable debug mode | 0 |
| `DATABASE_URL` | PostgreSQL connection string | Required |
| `JWT_SECRET_KEY` | Secret key for JWT signing | Required |
| `JWT_ACCESS_TOKEN_EXPIRES` | Token expiry in seconds | 3600 |
| `FLASK_RUN_PORT` | Flask development server port | 5000 |

### Gunicorn Configuration

The [gunicorn.conf.py](Backend/gunicorn.conf.py) file contains:
- Worker class: gevent
- Worker count: 4 (configurable based on CPU cores)
- Max requests: 1000
- Timeout: 60 seconds
- Port: 8000 (configurable)

### CORS Configuration

By default, CORS is enabled for all origins in development. For production, configure:

```python
CORS(app, origins=["https://yourdomain.com"])
```

## 📝 Logging

The application includes comprehensive logging:

- **Log File:** `logs/health_monitor.log`
- **Log Level:** INFO
- **Rotation:** 10MB per file, 10 file backup
- **Format:** `%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]`

Logged events include:
- Application startup
- User authentication (logins, token revocation)
- API requests/responses
- Database operations
- Error messages

## 📧 Contact & Support

For questions, support, or bug reports, please reach out to the project maintainer.
