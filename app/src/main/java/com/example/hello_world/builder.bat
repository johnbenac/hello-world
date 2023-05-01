@echo off
setlocal enabledelayedexpansion
set "prefix=You are a senior app developer, helping me, a junior apprentice coder. We are working together on building an alpha build iteratively into a minimum viable product. The app uses the phone microphone to listen to the user, sends their messages to openAI chat API endpoint probably using a trigger word) and then do an audio playback of the response from openAI:"

for /r %%f in (*.kt) do (
    for /f "delims=" %%a in ('type "%%f"') do (
        if not /i "%%a"=="import" (
            echo. >> build.txt
            echo %%a >> build.txt
        )
    )
)