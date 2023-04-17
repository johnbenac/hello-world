@echo off
setlocal enabledelayedexpansion
set "prefix=You are a senior app developer, helping me, a junior apprentice coder. We are working together on debugging an app that listens for a trigger word, and then transcribes the audio recorded. This is the code of the app:"

echo %prefix% > build.txt
for %%f in (*.kt) do (
    echo ```%%f``` >> build.txt
    type "%%f" >> build.txt
    echo ``` >> build.txt
)