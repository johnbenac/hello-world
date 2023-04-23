@echo off
setlocal enabledelayedexpansion

echo Enter the file list string provided by the AI code copilot:
set /p fileList="File list: "

set "files=%fileList: =;%"
echo Files to be compiled: %files%

echo. > build.txt
for %%f in (%files%) do (
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