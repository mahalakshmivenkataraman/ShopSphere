@echo off
setlocal
cd /d "%~dp0"
if not exist target\classes mkdir target\classes
if not exist target\test-classes mkdir target\test-classes
rem Use project-relative paths so spaces in the parent folder are safe.
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName.Substring((Get-Location).Path.Length + 1).Replace('\','/') } | Set-Content -Encoding ascii 'target/sources.txt'"
if errorlevel 1 (
  echo Could not prepare Java source list. Ensure PowerShell is available.
  exit /b 1
)
javac -encoding UTF-8 -d target\classes @target\sources.txt
if errorlevel 1 (
  echo Main source compilation failed.
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem 'src/test/java' -Recurse -Filter '*Runner.java' | ForEach-Object { $_.FullName.Substring((Get-Location).Path.Length + 1).Replace('\','/') } | Set-Content -Encoding ascii 'target/test-sources.txt'"
if errorlevel 1 (
  echo Could not prepare test source list.
  exit /b 1
)
javac -encoding UTF-8 -cp target\classes -d target\test-classes @target\test-sources.txt
if errorlevel 1 (
  echo Test runner compilation failed.
  exit /b 1
)
java -cp "target\classes;target\test-classes" ecommerce.test.TestRunner
if errorlevel 1 (
  echo One or more tests failed.
  exit /b 1
)
endlocal
