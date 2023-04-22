@echo off
setlocal enabledelayedexpansion
set "prefix=You are a senior app developer, helping me, a junior apprentice coder. We are working together on building an alpha build iteratively into a minimum viable product. The app uses the phone microphone to listen to the user, sends their messages to openAI chat API endpoint probably using a trigger word) and then do an audio playback of the response from openAI:"

echo Select a category:
echo 1. UI and User Interaction - Working with user interface components and handling user interactions
echo 2. Text-to-Speech Services - Implementing and troubleshooting text-to-speech functionality
echo 3. Assistant ViewModel and Conversation - Managing conversation state and assistant logic
echo 4. Settings and Profiles - Creating and managing settings and profiles for the app
echo 5. OpenAI API Integration - Integrating and working with the OpenAI API
echo 6. Voice Detection - Implementing and troubleshooting voice trigger detection
echo 7. Entire Codebase - Include all code files

set /p category="Enter the category number: "

if %category%==1 set "files=MainActivity.kt AssistantScreen.kt ConversationScreen.kt MessageCard.kt"
if %category%==2 set "files=MainActivity.kt AndroidTextToSpeechService.kt ElevenLabsTextToSpeechService.kt TextToSpeechService.kt"
if %category%==3 set "files=MainActivity.kt AssistantViewModel.kt ConversationMessage.kt"
if %category%==4 set "files=MainActivity.kt SettingsScreen.kt EditSettingsScreen.kt Profile.kt SettingsViewModel.kt"
if %category%==5 set "files=MainActivity.kt OpenAiApiService.kt OpenAiApiResponse.kt"
if %category%==6 set "files=MainActivity.kt VoiceTriggerDetector.kt"
if %category%==7 set "files=*.kt"

echo %prefix% > build.txt
for %%f in (%files%) do (
    echo. >> build.txt
    echo ```%%f``` >> build.txt
    echo. >> build.txt
    set "prevLine="
    set "importSection=false"
    for /f "usebackq delims=" %%l in ("%%f") do (
        set "curLine=%%l"
        if "!curLine:~0,6!"=="import" (
            if "!importSection!"=="false" (
                set "curLine=(additional import statements abridged)"
                set "importSection=true"
            ) else (
                set "curLine="
            )
        ) else (
            set "importSection=false"
        )
        if not "!curLine!"=="" (
            echo !curLine! >> build.txt
        )
        set "prevLine=!curLine!"
    )
    echo. >> build.txt