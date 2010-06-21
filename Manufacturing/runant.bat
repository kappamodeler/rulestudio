
set JAVA_HOME=C:\Program Files\Java\jdk1.5.0_21
rem "C:\Program Files\Java\jdk1.5.0_21\bin\java"
set PATH=%JAVA_HOME%\bin;%PATH%

rem ant -debug -lib libs/ant-contrib-1.0b3.jar -lib libs/commons-lang-2.0.jar -lib libs/jakarta-regexp-1.3.jar -lib libs/svnClientAdapter.jar -lib libs/svnant.jar -lib libs/svnjavahl.jar 

ant -lib libs/ant-contrib-0.6.jar -lib libs/ganymed.jar -lib libs/svnClientAdapter.jar -lib libs/svnant.jar -lib libs/svnjavahl.jar -lib libs/svnkit.jar


