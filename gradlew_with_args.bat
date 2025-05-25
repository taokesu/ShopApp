@echo off
echo Запуск Gradle с дополнительными JVM аргументами для Java 21...
set JAVA_OPTS=
for /f "tokens=*" %%a in (gradle.jvmargs) do (
  set JAVA_OPTS=!JAVA_OPTS! %%a
)

call gradlew %* --no-daemon
