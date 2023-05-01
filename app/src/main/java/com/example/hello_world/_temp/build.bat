@echo off
setlocal enabledelayedexpansion

set "root_path=C:\Users\john\AndroidStudioProjects\helloworld\app\src\main\java\com\example\hello_world"
set "prefix=this is the codebase:"
set "excluded_dirs=utils ui\theme"
set "skip_substrings=import package @compose"

echo %prefix% > build.txt
echo Processing the following files:

for /r "%root_path%" %%d in (.) do (
  set "skip_dir=false"
  for %%x in (%excluded_dirs%) do (
    if "%%~dpd"=="%root_path%\%%x" (
      set "skip_dir=true"
    )
  )
  if !skip_dir!==true (
    goto :skip_folder
  )
  for %%f in (%%d\*.kt) do (
    echo %%d\%%f
    echo. >> build.txt
    echo ```%%d\%%f.kt >> build.txt
    echo. >> build.txt
    for /f "tokens=* delims=" %%a in (%%f) do (
      set "line=%%a"
      set "skip_line=false"
      for %%s in (%skip_substrings%) do (
        if "!line:~0,%%~s!"=="%%s" (
          set "skip_line=true"
        )
      )
      if "!line!"=="!line: =!" (
        set "skip_line=true"
      )
      if "!skip_line!"=="false" (
        echo !line! >> build.txt
      )
    )
    echo. >> build.txt
    echo ``` >> build.txt
    echo. >> build.txt
  )
  :skip_folder
)

echo.
echo Done processing files. Press any key to exit.
pause >nul