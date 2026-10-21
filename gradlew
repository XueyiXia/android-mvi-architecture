#!/usr/bin/env sh
set -eu
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
APP_HOME="$DIR"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  echo "Gradle Wrapper JAR not found at $CLASSPATH" >&2
  echo "Android Studio will generate it when you run the wrapper task or use a local Gradle installation." >&2
  exit 1
fi
JAVA_OPTS="${JAVA_OPTS:--Xmx64m -Xms256m}"
exec "$JAVA_HOME/bin/java" $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
