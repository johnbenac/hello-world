@echo off
setlocal enabledelayedexpansion

echo Enter the list of keywords separated by spaces:
set /p keywordList="Keywords: "

echo These are all the files containing the keywords: %keywordList% > build.txt

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

        echo ^`^`^` >> build.txt
        echo ^`^`^`%%~nxf >> build.txt
        echo. >> build.txt
        set "prevLine="
        set "importSection=false"
        set "fileContent="
        for /f "usebackq delims=" %%l in ("%%f") do (
            set "fileContent=!fileContent!%%l__NEWLINE__"
        )
        set "fileContent=!fileContent:^`=__NEWLINE__!"
        echo !fileContent:__NEWLINE__=^

! >> build.txt
        echo. >> build.txt
    )
)

echo These were all the files containing the keywords: %keywordList% >> build.txt