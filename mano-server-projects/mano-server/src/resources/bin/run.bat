@echo off
rem Copyright (C) 2014 The MANO Authors. 
rem All rights reserved. Use is subject to license terms. 
rem
rem     http://mano.diosay.com/
rem

echo DIOSAY MANO Server (Version 1.1) 
echo (C) 2014 The MANO Authors. All rights reserved.


setlocal
set "CURRENT_DIR=%~dp0"
set "DRIVER=%~d0"
cd "%CURRENT_DIR%"
%DRIVER%
java -jar mano-server-1.0-SNAPSHOT.jar -encoding utf-8