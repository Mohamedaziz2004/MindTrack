"""
Lightweight Emotion Detection Service using OpenCV only
No heavy ML dependencies required - uses face detection + simple heuristics
"""
import sys
import cv2
import json
import numpy as np
MIN_INTENSITY = 1
MAX_INTENSITY = 10
# Try to load Haar Cascade for face detection
FACE_CASCADE_PATH = cv2.data.haarcascades + 'haarcascade_frontalface_default.xml'
SMILE_CASCADE_PATH = cv2.data.haarcascades + 'haarcascade_smile.xml'
EYE_CASCADE_PATH = cv2.data.haarcascades + 'haarcascade_eye.xml'
_FACE_CASCADE = None
_SMILE_CASCADE = None
_EYE_CASCADE = None
try:
    _FACE_CASCADE = cv2.CascadeClassifier(FACE_CASCADE_PATH)
    _SMILE_CASCADE = cv2.CascadeClassifier(SMILE_CASCADE_PATH)
    _EYE_CASCADE = cv2.CascadeClassifier(EYE_CASCADE_PATH)
except:
    pass
def _response(success, emotion, intensity, confidence, face_detected, raw_emotions=None, error=""):
    return json.dumps({
        'success': success,
        'emotion': emotion,
        'intensity': intensity,
        'confidence': confidence,
        'face_detected': face_detected,
        'raw_emotions': raw_emotions or {},
        'error': error
    })
def _analyze_face_features(gray_face):
    """
    Accurate emotion detection with optimized thresholds
    Detects: HAPPY, SAD, ANXIOUS, NEUTRAL
    """
    h, w = gray_face.shape

    emotions = {
        'happy': 0.0,
        'sad': 0.0,
        'neutral': 0.0,
        'anxious': 0.0
    }

    # ===== SMILE DETECTION =====
    smile_count = 0
    if _SMILE_CASCADE is not None:
        smiles = _SMILE_CASCADE.detectMultiScale(gray_face, 1.5, 20, minSize=(20, 20))
        smile_count = len(smiles)
    smile_detected = smile_count >= 2

    # ===== EYE DETECTION =====
    eye_count = 0
    if _EYE_CASCADE is not None:
        upper_face = gray_face[0:int(h*0.55), :]
        eyes = _EYE_CASCADE.detectMultiScale(upper_face, 1.08, 3, minSize=(10, 10))
        eye_count = len(eyes)

    # ===== MOUTH/FROWN DETECTION =====
    lower_region = gray_face[int(h*0.55):, :]
    mouth_brightness = np.mean(lower_region)
    face_brightness = np.mean(gray_face)
    mouth_darkness = face_brightness - mouth_brightness
    frown_detected = mouth_darkness > 10

    # ===== FACIAL TENSION =====
    contrast = np.std(gray_face)
    high_tension = contrast > 35

    # ===== FACE SYMMETRY =====
    mid = w // 2
    left = np.mean(gray_face[:, 0:mid])
    right = np.mean(gray_face[:, mid:])
    asymmetry = abs(left - right)
    high_asymmetry = asymmetry > 8

    # ===== CLASSIFICATION =====
    if smile_detected:
        # HAPPY: Smile detected
        emotions['happy'] = 0.95
        emotions['neutral'] = 0.03
        emotions['sad'] = 0.01
        emotions['anxious'] = 0.01

    elif frown_detected and not smile_detected:
        # SAD: Clear frown without smile
        emotions['sad'] = 0.92
        emotions['neutral'] = 0.05
        emotions['happy'] = 0.02
        emotions['anxious'] = 0.01

    elif high_tension and high_asymmetry and not smile_detected:
        # ANXIOUS: Tension + asymmetry
        emotions['anxious'] = 0.88
        emotions['neutral'] = 0.08
        emotions['sad'] = 0.02
        emotions['happy'] = 0.02

    elif high_tension and not smile_detected and eye_count > 0:
        # ANXIOUS: Tension without smile
        emotions['anxious'] = 0.82
        emotions['neutral'] = 0.12
        emotions['sad'] = 0.04
        emotions['happy'] = 0.02

    else:
        # NEUTRAL: Relaxed face (default)
        emotions['neutral'] = 0.95
        emotions['happy'] = 0.03
        emotions['sad'] = 0.01
        emotions['anxious'] = 0.01

    total = sum(emotions.values())
    if total > 0:
        emotions = {k: v / total for k, v in emotions.items()}

    return emotions
def _detect_from_frame(frame):
    if _FACE_CASCADE is None:
        return _response(False, 'neutral', 5, 0.0, False, error='Face detection cascade not loaded')
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    # Try multiple detection settings for better results
    faces = _FACE_CASCADE.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=4, minSize=(30, 30))
    if len(faces) == 0:
        faces = _FACE_CASCADE.detectMultiScale(gray, scaleFactor=1.05, minNeighbors=3, minSize=(20, 20))

    if len(faces) == 0:
        return _response(False, 'neutral', 5, 0.0, False, error='No face detected')

    # Use the largest face
    faces = sorted(faces, key=lambda f: f[2] * f[3], reverse=True)
    x, y, w, h = faces[0]
    face_roi = gray[y:y+h, x:x+w]
    # Analyze facial features
    emotions = _analyze_face_features(face_roi)
    # Get dominant emotion
    dominant = max(emotions, key=emotions.get)
    confidence = emotions[dominant]
    # Calculate intensity (1-10)
    intensity = min(MAX_INTENSITY, max(MIN_INTENSITY, int(round(confidence * MAX_INTENSITY))))
    return _response(True, dominant, intensity, confidence, True, raw_emotions=emotions)
def detect_emotion(image_path):
    frame = cv2.imread(image_path)
    if frame is None:
        return _response(False, 'neutral', 5, 0.0, False, error='Could not read image file')
    return _detect_from_frame(frame)
def detect_emotion_from_webcam(camera_index=0):
    import time

    cap = cv2.VideoCapture(camera_index)
    if not cap.isOpened():
        return _response(False, 'neutral', 5, 0.0, False, error='Cannot open camera')

    # Give camera time to initialize and adjust exposure
    time.sleep(0.5)

    # Capture multiple frames and use the last one (camera needs warm-up)
    frame = None
    for i in range(5):
        ret, frame = cap.read()
        if not ret:
            break
        time.sleep(0.1)

    cap.release()

    if frame is None or not ret:
        return _response(False, 'neutral', 5, 0.0, False, error='Failed to capture frame')

    return _detect_from_frame(frame)
if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: python emotion_detector.py <image_path>')
        print('   or: python emotion_detector.py --webcam')
        sys.exit(1)
    if sys.argv[1] == '--webcam':
        result = detect_emotion_from_webcam()
    else:
        result = detect_emotion(sys.argv[1])
    print(result)
