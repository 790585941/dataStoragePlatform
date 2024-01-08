@echo off

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
rem added double quotation marks to avoid the issue caused by the folder names containing spaces.
rem removed the last 5 chars(which means \bin\) to get the base DIR.
set BASE_DIR="%BASE_DIR:~0,-5%"

set CUSTOM_SEARCH_LOCATIONS=file:%BASE_DIR%/conf/

set SERVER=dataStoragePlatform-server

set "PLATFORM_JVM_OPTS=-Xms512m -Xmx512m -Xmn256m"

rem set dataStoragePlatform server options
set "PLATFORM_OPTS=%PLATFORM_OPTS% -jar %BASE_DIR%\target\%SERVER%.jar"

rem set dataStoragePlatform server spring config location
set "PLATFORM_CONFIG_OPTS=--spring.config.additional-location=%CUSTOM_SEARCH_LOCATIONS%"

rem set dataStoragePlatform server log4j file location
set "PLATFORM_LOG4J_OPTS=--logging.config=%BASE_DIR%/conf/dataStoragePlatform-server-logback.xml"


set COMMAND="%JAVA%" %PLATFORM_JVM_OPTS% %PLATFORM_OPTS% %PLATFORM_CONFIG_OPTS% %PLATFORM_LOG4J_OPTS% dataStoragePlatform.server %*

echo "dataStoragePlatform server is starting..."
rem start dataStoragePlatform server command
%COMMAND%
echo "dataStoragePlatform server is started!"

