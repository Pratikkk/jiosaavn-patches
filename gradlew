#!/bin/sh

# Gradle wrapper script
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=$(ulimit -H -n)
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD_LIMIT" = "unlimited" -o "$MAX_FD_LIMIT" = "1" ] ; then
            ulimit -n 1024
        elif [ $MAX_FD_LIMIT -lt 1024 ] ; then
            ulimit -n 1024
        fi
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_BASE_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# Split up the JVM_OPTS And GRADLE_OPTS values into an array
eval splitJvmOpts=($DEFAULT_JVM_OPTS $JVM_OPTS $GRADLE_OPTS)

exec "$JAVACMD" "${splitJvmOpts[@]}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"