set SERVICE_NAME=JMemcachedServer
set SERVICE_HOME=E:\servers\jmemcached-server
set PR_JVM=C:\Program Files\Java\jre1.8.0_171\bin\server\jvm.dll

set PR_INSTALL=%SERVICE_HOME%\prunsrv.exe

set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%SERVICE_HOME%\logs
set PR_STDOUTPUT=%SERVICE_HOME%\logs\stdout.txt
set PR_STDERROR=%SERVICE_HOME%\logs\stderr.txt
set PR_LOGLEVEL=Error

set PR_CLASSPATH=%SERVICE_HOME%\jmemcached-server-production-1.0.0.jar
set PR_JVMMS=256
set PR_JVMMX=1024
set PR_JVMMX=2048
set PR_JVMOPTIONS=-Dserver-prop=%SERVICE_HOME%\server.properties

set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=com.revenat.jmemcached.server.ServiceWrapper
set PR_STARTMETHOD=start
set PR_STOPMODE=jvm
set PR_STOPCLASS=com.revenat.jmemcached.server.ServiceWrapper
set PR_STOPMETHOD=stop

prunsrv.exe  //IS//%SERVICE_NAME%