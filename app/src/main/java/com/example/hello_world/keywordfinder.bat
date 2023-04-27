@echo off
setlocal enabledelayedexpansion

echo Enter the list of keywords separated by spaces:
set /p keywordList="Keywords: "

echo These are all the files containing the keywords: %keywordList% > build.txt
set "firstFile=true"

for /r %%f in (*.kt) do (
    set "found=false"
    for %%k in (%keywordList%) do (
        findstr /m /c:"%%k" "%%f" >nul 2>&1
        if !errorlevel!==0 (
            set "found=true"
            break
        )
    )
    if !found!==true (
        if !firstFile!==true (
            set "firstFile=false"
        ) else (
            echo ^`^`^` >> build.txt
            echo. >> build.txt
        )
        echo ^`^`^`%%~nxf >> build.txt
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
        echo ^`^`^` >> build.txt
        echo. >> build.txt
    )
)

echo These were all the files containing the keywords: %keywordList% >> build.txt