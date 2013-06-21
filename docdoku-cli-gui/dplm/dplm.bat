@ECHO OFF
SET JAVA_HOME="C:\Program Files\Java\jdk1.7.0_21\bin"
SET args=
:Boucle
  IF "%1"=="" GOTO Fin 
  SET args=%args% %1
  SHIFT
  GOTO Boucle
:Fin
%JAVA_HOME%\java -Xmx1024M -classpath %~dp0docdoku-cli-jar-with-dependencies.jar com.docdoku.cli.MainCommand%args%