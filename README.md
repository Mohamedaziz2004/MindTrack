# MindTrack JavaFX App

A Java 17 + JavaFX 17 desktop app scaffolded with Maven. It provides a main window with menu navigation, a list of entries, and a form to add new entries backed by an in-memory service.

## Project structure
- `src/main/java/org/mindtrack/mindtrackfxx/HelloApplication.java` — JavaFX `Application` entry, loads `main-view.fxml`.
- `src/main/java/org/mindtrack/mindtrackfxx/Launcher.java` — Plain `main` launcher.
- `src/main/java/org/mindtrack/mindtrackfxx/controller/*` — Controllers and shared service wiring.
- `src/main/java/org/mindtrack/mindtrackfxx/model/*` — Domain model (MindTrackEntry).
- `src/main/java/org/mindtrack/mindtrackfxx/service/*` — In-memory service.
- `src/main/resources/org/mindtrack/mindtrackfxx/view/*` — FXML views (main, entries list, new entry form).
- `src/test/java/.../service/MindTrackServiceTest.java` — Unit tests for the service.
- `pom.xml` — Maven config with JavaFX dependencies and plugin.

## Requirements
- JDK 17 installed and available.
- On Windows, set `JAVA_HOME` to your JDK 17 installation directory and add `%JAVA_HOME%\bin` to `PATH`.

Example (PowerShell):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Adjust the JDK path to match your installation.

## Build and run
Using the Maven wrapper in the project:

```powershell
cd "C:\Users\dhbeb\Documents\MindTrack\mindtrackFXX"
./mvnw clean package
./mvnw javafx:run
```

If you prefer running from IDE, use `Launcher.main`. With the Maven plugin, VM options are handled for you.

## Tests
Run unit tests:

```powershell
cd "C:\Users\dhbeb\Documents\MindTrack\mindtrackFXX"
./mvnw -Dtest=org.mindtrack.mindtrackfxx.service.MindTrackServiceTest test
```

## Next steps
- Persist entries (JSON file or SQLite).
- Add editing/deleting UI.
- Package a runtime image with `javafx-maven-plugin` jlink goals.
