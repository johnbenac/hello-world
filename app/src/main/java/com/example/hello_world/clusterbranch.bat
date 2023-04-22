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
echo 8. MainActivity and AssistantViewModel - Main activity and assistant view model interaction
echo 9. MainActivity and Text-to-Speech Services - Main activity and text-to-speech services interaction
echo 10. MainActivity and VoiceTriggerDetector - Main activity and voice trigger detector interaction
echo 11. AssistantViewModel and Text-to-Speech Services - Assistant view model and text-to-speech services interaction
echo 12. AssistantViewModel and OpenAI API Integration - Assistant view model and OpenAI API integration
echo 13. AssistantViewModel and VoiceTriggerDetector - Assistant view model and voice trigger detector interaction
echo 14. SettingsViewModel and EditSettingsScreen - Settings view model and edit settings screen interaction
echo 15. SettingsViewModel and Profile - Settings view model and profile data management
echo 16. OpenAiApiService and OpenAiApiResponse - OpenAI API service and response handling
echo 17. VoiceTriggerDetector and ConversationMessage - Voice trigger detector and conversation message interaction
echo 18. UI, AssistantViewModel, and Text-to-Speech Services - User interface, assistant view model, and text-to-speech services interaction
echo 19. UI, AssistantViewModel, and OpenAI API Integration - User interface, assistant view model, and OpenAI API integration
echo 20. UI, AssistantViewModel, and VoiceTriggerDetector - User interface, assistant view model, and voice trigger detector interaction

set /p category="Enter the category number: "

if %category%==1 set "files=MainActivity.kt AssistantScreen.kt ConversationScreen.kt MessageCard.kt"
if %category%==2 set "files=MainActivity.kt AndroidTextToSpeechService.kt ElevenLabsTextToSpeechService.kt TextToSpeechService.kt"
if %category%==3 set "files=MainActivity.kt AssistantViewModel.kt ConversationMessage.kt"
if %category%==4 set "files=MainActivity.kt SettingsScreen.kt EditSettingsScreen.kt Profile.kt SettingsViewModel.kt"
if %category%==5 set "files=MainActivity.kt OpenAiApiService.kt OpenAiApiResponse.kt"
if %category%==6 set "files=MainActivity.kt VoiceTriggerDetector.kt"
if %category%==7 set "files=*.kt"
if %category%==8 set "files=MainActivity.kt AssistantViewModel.kt"
if %category%==9 set "files=MainActivity.kt AndroidTextToSpeechService.kt ElevenLabsTextToSpeechService.kt TextToSpeechService.kt"
if %category%==10 set "files=MainActivity.kt VoiceTriggerDetector.kt"
if %category%==11 set "files=AssistantViewModel.kt AndroidTextToSpeechService.kt ElevenLabsTextToSpeechService.kt TextToSpeechService.kt"
if %category%==12 set "files=AssistantViewModel.kt OpenAiApiService.kt OpenAiApiResponse.kt"
if %category%==13 set "files=AssistantViewModel.kt VoiceTriggerDetector.kt"
if %category%==14 set "files=SettingsViewModel.kt EditSettingsScreen.kt"
if %category%==15 set "files=SettingsViewModel.kt Profile.kt"
if %category%==16 set "files=OpenAiApiService.kt OpenAiApiResponse.kt"
if %category%==17 set "files=VoiceTriggerDetector.kt ConversationMessage.kt"
if %category%==18 set "files=MainActivity.kt AssistantScreen.kt ConversationScreen.kt MessageCard.kt AssistantViewModel.kt AndroidTextToSpeechService.kt ElevenLabsTextToSpeechService.kt TextToSpeechService.kt"
if %category%==19 set "files=MainActivity.kt AssistantScreen.kt ConversationScreen.kt MessageCard.kt AssistantViewModel.kt OpenAiApiService.kt OpenAiApiResponse.kt"
if %category%==20 set "files=MainActivity.kt AssistantScreen.kt ConversationScreen.kt MessageCard.kt AssistantViewModel.kt VoiceTriggerDetector.kt"

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