@echo off
type venue-server\pom.xml | findstr /v "<n>" | findstr /v "</n>" > venue-server\pom.xml.tmp
powershell -Command "(Get-Content venue-server\pom.xml.tmp) -replace '<n>venue-server</n>', '<name>venue-server</name>' | Set-Content venue-server\pom.xml"
del venue-server\pom.xml.tmp
echo POM file fixed 