# MindTrack_v1.2

## Google Sign-In Setup

This project includes an optional Google Sign-In button for desktop OAuth.

### 1) Use a Desktop OAuth client
In Google Cloud Console, create an OAuth 2.0 **Desktop app** client (recommended for JavaFX).
The loopback redirect must be set to:

- `http://localhost:8080/callback`

### 2) Configure credentials
Set environment variables (preferred) or update `src/main/resources/google.properties`.

Environment variables:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET` (optional for Desktop app clients)
- `GOOGLE_REDIRECT_URI` (optional, defaults to `http://localhost:8080/callback`)

### 3) Run
Run the app normally and click "Sign in with Google" on the login screen.

### Notes
- Do not commit client secrets to source control.
- If you created a Web Application client, switch to a Desktop client to avoid bundling secrets.

