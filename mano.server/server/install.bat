@ echo off
title mano server install
set local ENABLEDELAYEDEXPANSION

echo %CLASSPATH%
SET CLASSPATH=
for /R .\bin\ext %%A in (*.jar) do (
SET CLASSPATH=%CLASSPATH%;%%A
)

SET CLASSPATH=.;%CLASSPATH%
echo current path
echo %CLASSPATH%
echo done
pause