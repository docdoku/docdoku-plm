@echo off
rem Vol  information is displayed if called with an argument, otherwise the drive letters are displayed
set INFO=%1

call :Drive A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
set INFO=
goto :EOF

:Drive
vol %1: > nul 2>nul
If  ERRORLEVEL 1 goto Next

if  not "X"%INFO%=="X" (vol %1:) else echo %1:
:Next
shift
if NOT "X"%1=="X" goto :Drive