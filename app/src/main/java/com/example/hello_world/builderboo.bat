@echo off

rem This batch file will filter a file and create a slim version of it.

rem Get the path of the file that was dropped.
set "file=%~1"

rem Create a new file with the suffix "slim".
rem The new file will be created in the same directory as the original file.
echo. > "%~dpn1.slim"

rem Open the original file for reading.
rem The file will be opened in text mode.
rem The file will be read line by line.
for /f "delims=" %%i in ('type "%file%"') do (

rem Check if the line starts with "import" or "package".
rem If the line does not start with "import" or "package",
rem then the line is not filtered out.
if not "%%i"=="import" && not "%%i"=="package" (

rem Write the line to the new file.
echo %%i >> "%~dpn1.slim"

)

)

rem Close the original file.

rem Pause so that the user can see the results.
pause