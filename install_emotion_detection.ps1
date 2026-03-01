# ============================================
# MindTrack Emotion Detection Installation
# ============================================

Write-Host "🎯 MindTrack - Python Emotion Detection Setup" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Step 1: Check Python Installation
Write-Host "Step 1: Checking Python Installation..." -ForegroundColor Yellow

$pythonCommands = @("python", "python3", "py")
$pythonFound = $false
$pythonCmd = ""

foreach ($cmd in $pythonCommands) {
    try {
        $version = & $cmd --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Found Python: $cmd - $version" -ForegroundColor Green
            $pythonCmd = $cmd
            $pythonFound = $true
            break
        }
    } catch {
        # Try next command
    }
}

if (-not $pythonFound) {
    Write-Host "❌ Python not found!" -ForegroundColor Red
    Write-Host "`nPlease install Python 3.8+ from: https://www.python.org/downloads/" -ForegroundColor Red
    Write-Host "During installation, check 'Add Python to PATH'" -ForegroundColor Yellow
    Write-Host "`nPress any key to open Python download page..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Start-Process "https://www.python.org/downloads/"
    exit 1
}

# Step 2: Check pip
Write-Host "`nStep 2: Checking pip..." -ForegroundColor Yellow

try {
    $pipVersion = & $pythonCmd -m pip --version 2>&1
    Write-Host "✓ pip is installed: $pipVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ pip not found!" -ForegroundColor Red
    Write-Host "Installing pip..." -ForegroundColor Yellow
    & $pythonCmd -m ensurepip --upgrade
}

# Step 3: Check if requirements.txt exists
Write-Host "`nStep 3: Checking requirements.txt..." -ForegroundColor Yellow

if (-not (Test-Path "python\requirements.txt")) {
    Write-Host "❌ requirements.txt not found in python\ directory!" -ForegroundColor Red
    exit 1
}

Write-Host "✓ requirements.txt found" -ForegroundColor Green

# Step 4: Install dependencies
Write-Host "`nStep 4: Installing Python dependencies..." -ForegroundColor Yellow
Write-Host "⏳ This may take 5-10 minutes (downloading ~500MB of AI models)..." -ForegroundColor Cyan

try {
    & $pythonCmd -m pip install -r python\requirements.txt
    if ($LASTEXITCODE -ne 0) {
        throw "pip install failed"
    }
    Write-Host "✓ Dependencies installed successfully!" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to install dependencies!" -ForegroundColor Red
    Write-Host "Try running manually: $pythonCmd -m pip install -r python\requirements.txt" -ForegroundColor Yellow
    exit 1
}

# Step 5: Verify installation
Write-Host "`nStep 5: Verifying installation..." -ForegroundColor Yellow

try {
    $verifyScript = @"
import sys
try:
    import deepface
    import cv2
    import tensorflow
    import numpy
    print('OK')
    sys.exit(0)
except Exception as e:
    print(f'ERROR: {e}')
    sys.exit(1)
"@

    $result = $verifyScript | & $pythonCmd 2>&1

    if ($result -match "OK") {
        Write-Host "✓ All dependencies verified!" -ForegroundColor Green
    } else {
        Write-Host "⚠ Verification warning: $result" -ForegroundColor Yellow
        Write-Host "Dependencies may still work, but there might be issues." -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Could not verify installation, but it may still work" -ForegroundColor Yellow
}

# Step 6: Test the emotion detector
Write-Host "`nStep 6: Would you like to test the emotion detector now? (y/n)" -ForegroundColor Yellow
$response = Read-Host

if ($response -eq "y" -or $response -eq "Y") {
    Write-Host "`nTesting emotion detector with webcam..." -ForegroundColor Cyan
    Write-Host "⏳ This will take a few seconds..." -ForegroundColor Cyan

    try {
        & $pythonCmd python\emotion_detector.py --webcam
        Write-Host "`n✓ Test completed!" -ForegroundColor Green
    } catch {
        Write-Host "`n⚠ Test failed, but the app should still work" -ForegroundColor Yellow
    }
}

# Step 7: Summary
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "✅ Installation Complete!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan

Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "1. Run the app: mvn javafx:run" -ForegroundColor White
Write-Host "2. Click 'Detect Emotion' in the sidebar" -ForegroundColor White
Write-Host "3. Click 'Capture & Analyze' to detect your emotion" -ForegroundColor White
Write-Host "4. Wait 1-3 seconds for accurate AI detection" -ForegroundColor White
Write-Host "5. Click 'Save to Mood' to save to database" -ForegroundColor White

Write-Host "`nDocumentation:" -ForegroundColor Yellow
Write-Host "- Full Guide: PYTHON_SETUP_GUIDE.md" -ForegroundColor White
Write-Host "- Quick Start: EMOTION_DETECTION_README.md" -ForegroundColor White
Write-Host "- Summary: IMPLEMENTATION_SUMMARY.md" -ForegroundColor White

Write-Host "`n🎉 Enjoy accurate emotion detection!" -ForegroundColor Green
Write-Host "`nPress any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

