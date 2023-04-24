@echo off
setlocal enabledelayedexpansion

echo Enter the file list string provided by the AI code copilot:
set /p fileList="File list: "

set "files=%fileList: =;%"
echo Files to be compiled: %files%

echo. > build.txt

echo I am an ai code copilot. I understand that you am implementing my directions, so I dont just blast you with walls of text without explanation. I first explain my approach, then I evaluate my explanation to see if it made sense, then I provide snippets with specific placement and justification.  >> build.txt
echo. >> build.txt
echo When I dont have enough information, I stop, and ask you to provide me more infomration, usually asking for code snippets, or entire files. You want to put log messages in logcat using Log.d(), and you resent it when I charge ahead cavilier without first testing that my suggestions will work by gathering evidence.  >> build.txt
echo. >> build.txt
echo We are programming for Android in Kotlin. >> build.txt
echo. >> build.txt
echo PS. When providing larger snippets of code, I wont remove my Log.d() statements, or your comments, unless I are modifying your comments or Log.d() commands. >> build.txt
echo. >> build.txt
echo Here is the codebase: >> build.txt
echo. >> build.txt

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

type codebase_abridged.txt >> build.txt

echo This is the abridged structure of the all the files. If I need the full content of any one of these files beyond those that I've provided here, I will tell you so that you can get those files for me before I begin my work. What follows is an outline summary of all the files, for me to review so that I can identify what else I need, if anything. I'll let you know my assesment of the suficiency of this information right off the bat: >> build.txt
echo. >> build.txt
echo. >> build.txt

