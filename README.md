# 🧠 MindTrack - Personal Wellness & Productivity Platform

<div align="center">

![MindTrack Logo](MindTrack-Gestion-des-Objectifs-Personnelles/MindTrack-Gestion-des-Objectifs-Personnelles/src/main/resources/logo.png)

**A comprehensive JavaFX application for personal growth, habit tracking, mood management, exercise planning, and goal achievement**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17--21-blue.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Academic-green.svg)](https://esprit.tn/)

</div>

---

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Technologies Used](#-technologies-used)
- [Project Modules](#-project-modules)
- [Advanced Features & APIs](#-advanced-features--apis)
- [Installation Guide](#-installation-guide)
- [Configuration](#-configuration)
- [Database Setup](#-database-setup)
- [Running the Application](#-running-the-application)
- [Team & Acknowledgments](#-team--acknowledgments)

---

## 🎯 About the Project

**MindTrack** is an innovative personal wellness and productivity management platform developed as part of the academic curriculum at **ESPRIT - École Supérieure Privée d'Ingénierie et de Technologies**. This comprehensive application integrates multiple modules to help users manage their mental health, track habits, plan exercises, and achieve personal goals through an intuitive JavaFX interface.

The platform combines modern software development practices with advanced AI-powered features, providing users with intelligent insights, emotional analysis, and personalized recommendations for personal growth.

### 🎓 Academic Context
This project was developed as part of the software engineering curriculum, demonstrating proficiency in:
- Object-Oriented Programming (OOP)
- Design Patterns (MVC, DAO, Service Layer)
- Database Management & Integration
- API Integration & REST Services
- AI/ML Integration
- User Experience Design

---

## 🛠 Technologies Used

### Core Technologies
- **Java 17** - Primary programming language
- **JavaFX 17-21** - Rich desktop UI framework
- **Maven** - Build automation and dependency management
- **MySQL 8.0+** - Relational database management
- **FXML** - UI markup language for JavaFX

### UI Libraries & Frameworks
- **ControlsFX** - Extended JavaFX controls
- **FormsFX** - Form building framework
- **ValidatorFX** - Input validation framework
- **BootstrapFX** - Bootstrap-inspired styling

### API Integrations & External Services
- **Cohere AI API** - Natural language processing and emotion analysis
- **AssemblyAI** - Speech-to-text transcription
- **MyMemory Translation API** - Language detection and translation
- **Open-Meteo API** - Weather information
- **Ollama (Llama 3.2)** - Local AI chatbot
- **Google OAuth 2.0** - Authentication
- **CompreFace** - Facial recognition

### AI/ML Technologies
- **OpenCV** - Computer vision and face detection
- **Python FER (Facial Emotion Recognition)** - Emotion detection
- **NumPy** - Numerical computing for ML
- **Pillow** - Image processing

### Additional Libraries
- **OkHttp3** - HTTP client for API requests
- **Gson** - JSON processing
- **Jackson** - JSON data binding
- **BCrypt** - Password hashing
- **Jakarta Mail** - Email services (SMTP)
- **Apache PDFBox** - PDF generation
- **OpenPDF** - PDF document creation
- **ZXing** - QR code generation/scanning
- **Webcam Capture** - Camera integration
- **JUnit 5** - Unit testing framework

---

## 📦 Project Modules

The MindTrack platform is organized into **five specialized modules**, each focusing on a specific aspect of personal wellness and productivity:

### 1️⃣ MindTrack - Gestion des Utilisateurs (User Management)
**Path**: `MindTrack-gestion-user (2)/MindTrack-gestion-user/`

**Description**: Comprehensive user authentication, authorization, and profile management system with advanced security features.

**Key Features**:
- 👤 **User Registration & Login** - Secure account creation with email verification
- 🔐 **Password Security** - BCrypt hashing with password reset via email OTP
- 🌐 **Google OAuth Integration** - Sign in with Google using OAuth 2.0
- 📸 **Facial Recognition Login** - Face-based authentication using CompreFace
- 👁️ **Profile Management** - Upload profile pictures, edit personal information
- 🧠 **Psychological Profile** - User personality assessment and profile
- 📧 **Email Services** - SMTP integration for notifications and password reset

**Entities**: `Utilisateur`, `ProfilPsychologique`

**Advanced Features**:
- Webcam capture for profile pictures
- QR code generation for user profiles
- Google OAuth 2.0 PKCE flow for desktop
- Facial recognition enrollment and verification
- Token-based password reset system

---

### 2️⃣ MindTrack - Gestion des Objectifs Personnels (Goals Management)
**Path**: `MindTrack-Gestion-des-Objectifs-Personnelles/MindTrack-Gestion-des-Objectifs-Personnelles/`

**Description**: Strategic goal setting and achievement tracking system with milestone management and progress visualization.

**Key Features**:
- 🎯 **Goal Creation & Management** - Define personal and professional objectives
- 🏁 **Milestone Tracking** - Break down goals into achievable milestones (jalons)
- 📊 **Progress Dashboard** - Visual progress indicators and analytics
- 📅 **Action Plans** - Detailed planification with tasks and deadlines
- 📈 **Insights & Analytics** - Performance metrics and achievement statistics
- 📄 **PDF Export** - Generate reports of goals and progress
- 📧 **Email Notifications** - Reminders and milestone completion alerts

**Entities**: `Objectif`, `Jalon`, `PlanAction`, `Planificateur`

**Views**:
- Dashboard View - Overview of all goals and progress
- Objectif View - Goal creation and editing
- Jalon Progression View - Milestone tracking
- Plan Action View - Action plan management
- Insights View - Analytics and reports

---

### 3️⃣ MindTrack - Gestion des Exercices (Exercise Management)
**Path**: `MindTrack-gestion-exercices (1)/MindTrack-gestion-exercices/`

**Description**: Complete exercise planning and workout tracking system with gamification elements and progress analytics.

**Key Features**:
- 💪 **Exercise Library** - Create and manage exercise database
- 📋 **Session Planning** - Build workout sessions with multiple exercises
- ⏱️ **Session Tracking** - Real-time workout session monitoring
- 📊 **Statistics & Analytics** - Track performance metrics over time
- 🏆 **Badges & Achievements** - Gamification with unlockable badges
- ✅ **Todo Management** - Task lists for workout planning
- 📜 **History** - Complete workout history and records
- 📈 **Progress Tracking** - Monitor improvements and set new records

**Entities**: `Exercice`, `Session`, `Progression`, `Todo`

**Controllers**:
- `TableauBordController` - Main dashboard
- `ListeExercicesController` - Exercise catalog
- `DemarrerSessionController` - Session start/stop
- `StatistiquesController` - Performance analytics
- `BadgesController` - Achievement system
- `MissionShellController` - Challenge system

**Advanced Features**:
- Real-time session timer with pause/resume
- Automatic progression calculation
- Achievement unlock system
- Exercise difficulty levels
- Custom workout routines

---

### 4️⃣ MindTrack - Gestion de l'Humeur (Mood Management)
**Path**: `MindTrack-gestion-humeur (3)/MindTrack-gestion-humeur/`

**Description**: Advanced mood tracking and emotional wellness system with AI-powered analysis and emotion detection.

**Key Features**:
- 😊 **Mood Tracking** - Log daily emotional states
- 📝 **Emotional Journal** - Write detailed journal entries
- 🤖 **AI Analysis** - Cohere AI-powered emotion analysis and suggestions
- 🎤 **Voice Journaling** - Audio recording with speech-to-text transcription
- 📸 **Emotion Detection** - Real-time facial emotion recognition via webcam
- 🌍 **Multi-language Support** - Automatic language detection and translation
- 📊 **Mood Statistics** - Track emotional patterns over time
- 💡 **AI Suggestions** - Personalized wellness recommendations

**Entities**: `Humeur`, `JournalEmotionnel`

**API Integrations**:
- **Cohere AI (c4ai-aya-23-8b)** - Emotion analysis and sentiment detection
- **AssemblyAI** - Speech-to-text transcription
- **MyMemory API** - Language detection and translation to English
- **Python FER + OpenCV** - Real-time facial emotion recognition

**Advanced Features (Métiers Avancés)**:
- Real-time emotion detection from webcam feed
- Audio recording and transcription to text
- AI-powered journal entry analysis with confidence scores
- Emotion classification (sadness, joy, anger, anxiety, stress, fear, surprise, disgust, hope, neutral)
- Sentiment analysis (positive, negative, neutral)
- Personalized coping suggestions based on emotional state
- Multi-language journal support with automatic translation

**Python Integration**:
- `emotion_detector.py` - Facial emotion recognition using FER library
- OpenCV integration for camera access
- Real-time emotion prediction with confidence levels

---

### 5️⃣ MindTrack - Suivi des Habitudes (Habit Tracking)
**Path**: `MindTrack-Suivi-des-habitudes (1)/MindTrack-Suivi-des-habitudes/`

**Description**: Intelligent habit formation and tracking system with smart reminders and weather-aware suggestions.

**Key Features**:
- ✅ **Habit Creation** - Define custom habits with frequency goals
- 📅 **Daily Tracking** - Check-in system for habit completion
- 🔔 **Smart Reminders** - Intelligent notification system
- 🌤️ **Weather Integration** - Weather-aware habit suggestions
- 🤖 **AI Chatbot** - Motivation and habit advice assistant
- 📊 **Streak Tracking** - Monitor consistency and build streaks
- 📈 **Progress Analytics** - Visualize habit formation over time
- 📄 **PDF Reports** - Export habit tracking data

**Entities**: `Habitude`, `SuiviHabitude`, `RappelHabitude`

**API Integrations**:
- **Open-Meteo API** - Real-time weather data for location-based suggestions
- **Ollama (Llama 3.2:1b)** - Local AI chatbot for motivation and advice

**Advanced Features (Métiers Avancés)**:
- **Smart Reminder System** - Context-aware notifications based on:
  - Time of day
  - Weather conditions
  - User's typical completion patterns
  - Habit difficulty and frequency
- **Weather-Based Recommendations** - Suggests outdoor/indoor habits based on current weather
- **AI Motivation Coach** - Ollama-powered chatbot for:
  - Habit formation advice
  - Motivation messages
  - Overcoming obstacles
  - Personalized suggestions
- **Streak Analysis** - Identifies patterns and optimal timing
- **PDF Export** - Generate comprehensive habit reports

**Services**:
- `WeatherService` - Fetch weather data for Tunis (configurable)
- `ChatbotService` - Interface with local Ollama LLM
- `SmartReminderService` - Intelligent notification scheduling
- `MotivationService` - Generate encouraging messages

---

## 🚀 Advanced Features & APIs

### AI & Machine Learning Integration

#### 1. **Cohere AI Integration** (Mood Management)
- **API**: Cohere AI (c4ai-aya-23-8b model)
- **Purpose**: Advanced emotion analysis and sentiment detection
- **Features**:
  - Natural language understanding of journal entries
  - Emotion classification with confidence scores
  - Sentiment analysis (positive/negative/neutral)
  - Personalized coping suggestions
  - Multi-turn conversational analysis

#### 2. **Facial Emotion Recognition** (Mood Management)
- **Technology**: Python FER + OpenCV
- **Features**:
  - Real-time emotion detection from webcam
  - 7 emotion categories (angry, disgust, fear, happy, sad, surprise, neutral)
  - Confidence scoring for each emotion
  - Integration with JavaFX via Python subprocess

#### 3. **Speech-to-Text** (Mood Management)
- **API**: AssemblyAI
- **Features**:
  - Audio file transcription
  - Support for voice journaling
  - High accuracy speech recognition
  - Automatic punctuation

#### 4. **Translation Services** (Mood Management)
- **API**: MyMemory Translation API
- **Features**:
  - Automatic language detection
  - Translation to English for analysis
  - Support for Arabic, French, English, and more
  - Character-based language detection

#### 5. **Local AI Chatbot** (Habit Tracking)
- **Technology**: Ollama with Llama 3.2:1b model
- **Features**:
  - Local inference (no cloud dependency)
  - Habit formation advice
  - Motivational messages
  - Personalized recommendations

### External Services Integration

#### 6. **Weather API** (Habit Tracking)
- **API**: Open-Meteo (Free, no authentication)
- **Features**:
  - Current weather conditions
  - Location-based weather (default: Tunis)
  - Weather code interpretation
  - Integration with habit recommendations

#### 7. **Google OAuth 2.0** (User Management)
- **Protocol**: OAuth 2.0 with PKCE flow
- **Features**:
  - Sign in with Google
  - Desktop application authentication
  - Secure token exchange
  - User profile retrieval

#### 8. **Facial Recognition** (User Management)
- **Technology**: CompreFace API
- **Features**:
  - Face enrollment
  - Face verification for login
  - Subject-based face matching
  - Image-based authentication

#### 9. **Email Services** (User Management, Goals)
- **Protocol**: SMTP (Gmail)
- **Features**:
  - Password reset emails with OTP
  - Goal milestone notifications
  - Account verification
  - Gmail App Password support

---

## 📥 Installation Guide

### Prerequisites

Before installing MindTrack, ensure you have the following installed on your system:

1. **Java Development Kit (JDK) 17 or higher**
   ```powershell
   java -version
   ```
   Download from: https://www.oracle.com/java/technologies/downloads/

2. **Apache Maven 3.6+**
   ```powershell
   mvn -version
   ```
   Download from: https://maven.apache.org/download.cgi

3. **MySQL Server 8.0+**
   ```powershell
   mysql --version
   ```
   Download from: https://dev.mysql.com/downloads/mysql/

4. **Git** (for cloning the repository)
   ```powershell
   git --version
   ```
   Download from: https://git-scm.com/downloads

5. **Python 3.8+** (for emotion detection features)
   ```powershell
   python --version
   ```
   Download from: https://www.python.org/downloads/

6. **Ollama** (optional, for AI chatbot in habit tracking)
   ```powershell
   ollama --version
   ```
   Download from: https://ollama.ai/download

### Step 1: Clone the Repository

```powershell
cd "C:\Users\YourUsername\Downloads"
git clone <repository-url> MindTrack
cd MindTrack
```

### Step 2: Database Setup

1. **Start MySQL Server**
   ```powershell
   # Windows - Start MySQL service
   net start MySQL80
   ```

2. **Create Database**
   ```powershell
   mysql -u root -p
   ```
   
   In MySQL console:
   ```sql
   CREATE DATABASE mindtrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE mindtrack;
   ```

3. **Import Database Schema**
   
   Import the SQL file from Goals Management module:
   ```powershell
   mysql -u root -p mindtrack < "MindTrack-Gestion-des-Objectifs-Personnelles/MindTrack-Gestion-des-Objectifs-Personnelles/src/main/resources/mindtrack-1 (2).sql"
   ```

4. **Add Password Reset Table** (for User Management)
   ```sql
   CREATE TABLE password_reset_tokens (
       id INT AUTO_INCREMENT PRIMARY KEY,
       email VARCHAR(255) NOT NULL,
       token VARCHAR(6) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       expires_at TIMESTAMP NOT NULL,
       used BOOLEAN DEFAULT FALSE,
       INDEX idx_email (email),
       INDEX idx_token (token)
   );
   ```

5. **Add Profile Picture Column** (for User Management)
   ```sql
   ALTER TABLE utilisateur
   ADD COLUMN profile_picture_path VARCHAR(512) NULL;
   ```

### Step 3: Python Environment Setup (for Emotion Detection)

1. **Navigate to Python directory**
   ```powershell
   cd "MindTrack-gestion-humeur (3)/MindTrack-gestion-humeur/python"
   ```

2. **Create virtual environment** (recommended)
   ```powershell
   python -m venv venv
   .\venv\Scripts\Activate.ps1
   ```

3. **Install Python dependencies**
   ```powershell
   pip install -r requirements.txt
   ```

   Or manually:
   ```powershell
   pip install opencv-python>=4.8.0 numpy>=1.24.0 requests>=2.28.0 fer>=23.0.0 Pillow>=9.0.0
   ```

### Step 4: Ollama Setup (Optional - for AI Chatbot)

1. **Install Ollama** from https://ollama.ai/download

2. **Pull Llama 3.2 model**
   ```powershell
   ollama pull llama3.2:1b
   ```

3. **Start Ollama service**
   ```powershell
   ollama serve
   ```
   Keep this running in the background when using the chatbot feature.

---

## ⚙️ Configuration

### Database Configuration

Each module needs database connection configuration. Update the connection details in the respective database utility classes:

**Typical configuration** (adjust according to your setup):
```java
// Database connection parameters
private static final String URL = "jdbc:mysql://localhost:3306/mindtrack";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

### API Keys Configuration

#### 1. Cohere AI API (Mood Management)
Create or update the API key in `JournalAnalysisService.java`:
```java
private static final String COHERE_API_KEY = "your_cohere_api_key";
```
Get your free API key at: https://cohere.com/

#### 2. AssemblyAI API (Mood Management)
Update in `SpeechToTextService.java`:
```java
private static final String ASSEMBLY_AI_API_KEY = "your_assemblyai_api_key";
```
Get your free API key at: https://www.assemblyai.com/

#### 3. Google OAuth (User Management)
Create `compreface.properties` OR set environment variables:

**Environment Variables**:
```powershell
$env:GOOGLE_CLIENT_ID="your_google_client_id"
$env:GOOGLE_CLIENT_SECRET="your_google_client_secret"
$env:GOOGLE_REDIRECT_URI="http://localhost:8080/callback"
```

**Or create `.env` file** in project root:
```properties
google.clientId=your_google_client_id
google.clientSecret=your_google_client_secret
google.redirectUri=http://localhost:8080/callback
```

Get credentials at: https://console.cloud.google.com/

#### 4. CompreFace API (User Management)
Update in `ComprefaceConfig.java` or set environment variables:
```properties
COMPREFACE_BASE_URL=http://localhost:8000
COMPREFACE_API_KEY=your_compreface_api_key
```

#### 5. Gmail SMTP (User Management)
Create `smtp.properties` in project root:
```properties
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_gmail_app_password
SMTP_FROM=your_email@gmail.com
SMTP_TLS=true
SMTP_AUTH=true
```

**Important**: Use Gmail App Password, not your regular password.
Generate at: https://myaccount.google.com/apppasswords

---

## 🚀 Running the Application

### Option 1: Run Individual Modules

Each module can be run independently using Maven:

#### User Management Module
```powershell
cd "MindTrack-gestion-user (2)/MindTrack-gestion-user"
mvn clean javafx:run
```

#### Goals Management Module
```powershell
cd "MindTrack-Gestion-des-Objectifs-Personnelles/MindTrack-Gestion-des-Objectifs-Personnelles"
mvn clean javafx:run
```

#### Exercise Management Module
```powershell
cd "MindTrack-gestion-exercices (1)/MindTrack-gestion-exercices"
mvn clean javafx:run
```

#### Mood Management Module
```powershell
cd "MindTrack-gestion-humeur (3)/MindTrack-gestion-humeur"
mvn clean javafx:run
```

#### Habit Tracking Module
```powershell
cd "MindTrack-Suivi-des-habitudes (1)/MindTrack-Suivi-des-habitudes"
mvn clean javafx:run
```

### Option 2: Build and Run with JAR

1. **Build the module**
   ```powershell
   mvn clean package
   ```

2. **Run the JAR**
   ```powershell
   java -jar target/MindTrack-1.0-SNAPSHOT.jar
   ```

### Option 3: Run from IDE

1. **Import project** in IntelliJ IDEA or Eclipse
2. **Configure Maven** to sync dependencies
3. **Run the main class** for each module:
   - User Management: `main.MainApp`
   - Goals: `org.example.MainFx`
   - Exercises: `application.MainApp`
   - Mood: `org.mindtrack.HelloApplication`
   - Habits: `main.AppM`

### Troubleshooting

**If you encounter JavaFX errors:**
```powershell
# Add VM options when running:
--module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.media
```

**If MySQL connection fails:**
- Verify MySQL service is running
- Check database name, username, and password
- Ensure MySQL port 3306 is not blocked by firewall

**If Python emotion detection fails:**
- Ensure Python virtual environment is activated
- Verify all Python packages are installed
- Check webcam permissions

**If Ollama chatbot doesn't work:**
- Ensure Ollama service is running: `ollama serve`
- Verify Llama 3.2:1b model is pulled
- Check localhost:11434 is accessible

---

## 👥 Team & Acknowledgments

### 🎓 Academic Institution

This project was developed at:

**ESPRIT - École Supérieure Privée d'Ingénierie et de Technologies**
- Website: https://esprit.tn/
- Location: Tunis, Tunisia
- Program: Software Engineering

### 🙏 Remerciements (Acknowledgments)

We would like to express our sincere gratitude to:

- **ESPRIT Administration** - For providing the resources and environment for this project
- **Our Academic Supervisors** - For their guidance, support, and valuable feedback throughout the development process
- **Our Professors** - For teaching us the fundamental concepts and best practices in software engineering
- **The ESPRIT Community** - For fostering an environment of innovation and collaboration
- **Open Source Communities** - For the excellent libraries and frameworks that made this project possible
- **API Providers** - Cohere AI, AssemblyAI, Google, Open-Meteo, and others for their powerful services
- **Our Families and Friends** - For their unwavering support and encouragement

### 💡 Project Vision

MindTrack represents our commitment to leveraging technology for mental wellness and personal growth. We believe that with the right tools and insights, everyone can achieve their goals and maintain a healthy, balanced lifestyle.

### 📬 Contact & Support

For questions, suggestions, or contributions, please contact:
- **Institution**: ESPRIT - École Supérieure Privée d'Ingénierie et de Technologies
- **Location**: Tunis, Tunisia

---

## 📄 License

This project is developed for academic purposes as part of the ESPRIT curriculum. All rights reserved.

**Academic Use Only** - Not for commercial distribution.

---

## 🌟 Features Summary

| Module | Key Features | Technologies | APIs |
|--------|-------------|--------------|------|
| **User Management** | Authentication, OAuth, Face Recognition, Profile Management | JavaFX, BCrypt, Webcam, QR Codes | Google OAuth, CompreFace, Gmail SMTP |
| **Goals Management** | Goal Setting, Milestones, Action Plans, Analytics, PDF Export | JavaFX, OpenPDF, Jakarta Mail | Email Notifications |
| **Exercise Management** | Workout Tracking, Sessions, Badges, Statistics, History | JavaFX, JSON Processing | None (Standalone) |
| **Mood Management** | Emotion Tracking, Journal, AI Analysis, Voice Recording, Face Detection | JavaFX, Python, OpenCV, FER | Cohere AI, AssemblyAI, MyMemory |
| **Habit Tracking** | Habit Formation, Smart Reminders, Weather Integration, AI Chatbot | JavaFX, Jackson, PDFBox | Open-Meteo, Ollama (Llama 3.2) |

---

<div align="center">

**Built with ❤️ by ESPRIT Engineering Students**

*Empowering personal growth through technology*

</div>

