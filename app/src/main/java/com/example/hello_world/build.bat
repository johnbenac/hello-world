@echo off
setlocal enabledelayedexpansion
set "prefix=You are a senior app developer, helping me, a junior apprentice coder. We are working together on building an alpha build iterativly into a minimum viable product. The app uses the phone microphone to listen to the user, sends their messages to openAI chat API endpoint probably using a trigger word) and then do an audio playback of the response from openAI:"

echo %prefix% > build.txt
for %%f in (*.kt) do (
    echo ```%%f``` >> build.txt
    type "%%f" >> build.txt
    echo ``` >> build.txt
)