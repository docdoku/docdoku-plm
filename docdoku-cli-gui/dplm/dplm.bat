@ECHO OFF
rem Enter here the path to the JDK or JRE directory bin
rem SET JAVA_HOME="C:\Program Files\Java\jdk1.7.0_21\bin"
set basedir=%~dp0
set cp=%basedir%\docdoku-cli-jar-with-dependencies.jar

SET args=
:Boucle
  IF "%1"=="" GOTO Fin 
  SET args=%args% %1
  SHIFT
  GOTO Boucle
:Fin
%JAVA_HOME%\java -Xmx1024M -cp %cp% com.docdoku.cli.MainCommand%args%