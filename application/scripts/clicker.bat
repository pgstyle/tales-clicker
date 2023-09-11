@echo off

where java 2> nul > nul
if not %errorlevel% == 0 ( 
    echo Error: JRE not found
    echo Java Runtime Environment is required to execute the Tales Clicker.
    echo Please install the Java Runtime Environment of your platform.
) else (
    java -Xmx120M -jar %~dp0/tales-clicker.jar %*
)
