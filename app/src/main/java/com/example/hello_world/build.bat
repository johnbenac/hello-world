@echo off
setlocal enabledelayedexpansion

set "prefix=this is the codebase:"
set "skipDirs=utils ui/theme"

echo.
echo Debugging: Check command line argument
echo Argument 1: "%1%"
pause

if "%1"=="" (
    set "inputDir=%CD%"
) else (
    set "inputDir=%~f1"
)

set "outputFile=%inputDir%\build.txt"

echo.
echo Debugging: Check input directory and output file location
echo Input Directory: "%inputDir%"
echo Output File: "%outputFile%"
pause

echo %prefix% > "%outputFile%"
set "fileCount=0"
set "lineCount=0"

for /r "%inputDir%" %%f in (*.kt) do (
    set "skip=0"
    for %%d in (%skipDirs%) do (
        echo "%%f" | findstr /C:"\%%d\" >nul && set "skip=1"
    )
    if !skip! == 0 (
        echo. >> "%outputFile%"
        echo ```%%~nf.kt >> "%outputFile%"   
        echo. >> "%outputFile%"
        type "%%f" | findstr /B /V "import" >> "%outputFile%"
        echo. >> "%outputFile%"
        echo ``` >> "%outputFile%"
        echo. >> "%outputFile%"
        
        REM Debugging: Print the file being processed
        echo Processing: "%%f"
        set /a fileCount+=1
        for /f %%l in ('type "%%f" ^| find /c /v ""') do set /a lineCount+=%%l
    )
)

REM Debugging: Print summary info
echo.
echo Summary:
echo Files processed: %fileCount%
echo Output file location: "%outputFile%"
echo Output file line count: %lineCount%
pause