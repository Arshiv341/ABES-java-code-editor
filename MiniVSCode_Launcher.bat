@echo off
title MiniVSCode Multi Version Launcher
color 0A

echo Searching for MiniVSCodeProject folder...
echo.

:: Search in common drives
set "projectPath="

for %%D in (C D E F) do (
    if exist "%%D:\MiniVSCodeProject" (
        set "projectPath=%%D:\MiniVSCodeProject"
        goto found
    )
)

:found
if "%projectPath%"=="" (
    echo MiniVSCodeProject folder not found!
    pause
    exit
)

cd /d "%projectPath%"

echo ======================================
echo        MiniVSCode Launcher
echo       Developer Ritik Tiwari
echo ======================================
echo.
echo Project Found At:
echo %projectPath%
echo.
echo Select Version to Run:
echo.
echo 1 - Version 0.1 (Manual Compile)
echo 2 - Version 0.2 (With lib folder)
echo 3 - Version 2 (Jar - Stable)
echo.

set /p choice=Enter your choice (1/2/3):

if "%choice%"=="1" goto v1
if "%choice%"=="2" goto v2
if "%choice%"=="3" goto v3
goto end

:v1
cd /d "%projectPath%\src"
"C:\Program Files\Java\jdk-24\bin\javac.exe" --module-path "%projectPath%\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml DSAtraing\MiniVSCode.java
java --module-path "%projectPath%\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml DSAtraing.MiniVSCode
goto end

:v2
cd /d "%projectPath%"
javac --module-path "javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml -cp ".;lib/*" -d out src\DSAtraing\MiniVSCode.java
java --module-path "javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml -cp ".;lib/*;out" DSAtraing.MiniVSCode
goto end

:v3
cd /d "%projectPath%\dist"
java --module-path "%projectPath%\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml -jar MiniVSCode.jar
goto end

:end
echo.
echo Done.
pause