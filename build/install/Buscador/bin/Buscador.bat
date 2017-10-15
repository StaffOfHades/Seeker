@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Buscador startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and BUSCADOR_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\Buscador.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\kotlin-stdlib-jre8-1.1.51.jar;%APP_HOME%\lib\kotlin-stdlib-jre7-1.1.51.jar;%APP_HOME%\lib\kotlin-stdlib-1.1.51.jar;%APP_HOME%\lib\mysql-connector-java-5.1.44.jar;%APP_HOME%\lib\mariadb-java-client-2.1.2.jar;%APP_HOME%\lib\jfreechart-1.0.19.jar;%APP_HOME%\lib\jcommon-1.0.16.jar;%APP_HOME%\lib\renjin-script-engine-0.8.2468.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\jcommon-1.0.23.jar;%APP_HOME%\lib\methods-0.8.2468.jar;%APP_HOME%\lib\stats-0.8.2468.jar;%APP_HOME%\lib\datasets-0.8.2468.jar;%APP_HOME%\lib\utils-0.8.2468.jar;%APP_HOME%\lib\graphics-0.8.2468.jar;%APP_HOME%\lib\grDevices-0.8.2468.jar;%APP_HOME%\lib\renjin-gnur-runtime-0.8.2468.jar;%APP_HOME%\lib\renjin-core-0.8.2468.jar;%APP_HOME%\lib\compiler-0.8.2468.jar;%APP_HOME%\lib\all-1.1.2.pom;%APP_HOME%\lib\renjin-appl-0.8.2468.jar;%APP_HOME%\lib\renjin-nmath-0.8.2468.jar;%APP_HOME%\lib\renjin-lapack-0.8.2468.jar;%APP_HOME%\lib\commons-math-2.2.jar;%APP_HOME%\lib\renjin-blas-0.8.2468.jar;%APP_HOME%\lib\renjin-math-common-0.8.2468.jar;%APP_HOME%\lib\gcc-runtime-0.8.2468.jar;%APP_HOME%\lib\netlib-native_ref-osx-x86_64-1.1-natives.jar;%APP_HOME%\lib\netlib-native_ref-linux-x86_64-1.1-natives.jar;%APP_HOME%\lib\netlib-native_ref-linux-i686-1.1-natives.jar;%APP_HOME%\lib\netlib-native_ref-win-x86_64-1.1-natives.jar;%APP_HOME%\lib\netlib-native_ref-win-i686-1.1-natives.jar;%APP_HOME%\lib\netlib-native_ref-linux-armhf-1.1-natives.jar;%APP_HOME%\lib\native_ref-java-1.1.jar;%APP_HOME%\lib\netlib-native_system-osx-x86_64-1.1-natives.jar;%APP_HOME%\lib\netlib-native_system-linux-x86_64-1.1-natives.jar;%APP_HOME%\lib\netlib-native_system-linux-i686-1.1-natives.jar;%APP_HOME%\lib\netlib-native_system-linux-armhf-1.1-natives.jar;%APP_HOME%\lib\netlib-native_system-win-x86_64-1.1-natives.jar;%APP_HOME%\lib\netlib-native_system-win-i686-1.1-natives.jar;%APP_HOME%\lib\native_system-java-1.1.jar;%APP_HOME%\lib\core-1.1.2.jar;%APP_HOME%\lib\commons-vfs2-2.0.jar;%APP_HOME%\lib\commons-compress-1.4.1.jar;%APP_HOME%\lib\joda-time-2.0.jar;%APP_HOME%\lib\renjin-asm-5.0.4b.jar;%APP_HOME%\lib\renjin-guava-17.0b.jar;%APP_HOME%\lib\codemodel-2.6.jar;%APP_HOME%\lib\arpack_combined_all-0.1.jar;%APP_HOME%\lib\commons-logging-1.1.1.jar;%APP_HOME%\lib\maven-scm-provider-svnexe-1.4.jar;%APP_HOME%\lib\maven-scm-provider-svn-commons-1.4.jar;%APP_HOME%\lib\maven-scm-api-1.4.jar;%APP_HOME%\lib\xz-1.0.jar;%APP_HOME%\lib\plexus-utils-1.5.6.jar;%APP_HOME%\lib\regexp-1.3.jar;%APP_HOME%\lib\jniloader-1.1.jar

@rem Execute Buscador
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %BUSCADOR_OPTS%  -classpath "%CLASSPATH%" buscador.Buscador %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable BUSCADOR_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%BUSCADOR_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
