@echo off
setlocal enabledelayedexpansion

set "prefix=You are a senior app developer, helping me, a junior apprentice coder. We are working together on building an alpha build iteratively into a minimum viable product. The app uses the phone microphone to listen to the user, sends their messages to openAI chat API endpoint probably using a trigger word) and then do an audio playback of the response from openAI:"

echo Enter the file list string provided by the AI code copilot:
set /p fileList="File list: "

set "files=%fileList: =;%"
echo Files to be compiled: %files%

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
)