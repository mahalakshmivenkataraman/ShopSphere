@echo off
setlocal
cd /d "%~dp0"
if not exist target mkdir target
if not exist target\classes mkdir target\classes
rem Build a javac argument file with project-relative paths (no spaces).
rem This safely handles spaces in the parent folder path.
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName.Substring((Get-Location).Path.Length + 1).Replace('\','/') } | Set-Content -Encoding ascii 'target/sources.txt'"
if errorlevel 1 (
  echo Could not prepare Java source list. Ensure PowerShell is available.
  exit /b 1
)
javac -encoding UTF-8 -d target\classes @target\sources.txt
if errorlevel 1 (
  echo Compilation failed. Check the Java error messages above.
  exit /b 1
)
java -cp target\classes ecommerce.main.Main
endlocal
