@echo off
cd /d "%~dp0"
"..\.tools\apache-maven-3.9.10\bin\mvn.cmd" spring-boot:run -q
