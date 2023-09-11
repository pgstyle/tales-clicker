#!/usr/bin/env sh

which java 2>&1 1>/dev/null || ! (
    echo "Error: JRE not found" >&2
    echo "Java Runtime Environment is required to execute the Tales Clicker." >&2
    echo "Please install the Java Runtime Environment of your platform." >&2
) && java -Xmx120M -jar ${0%/*}/tales-clicker.jar "$@"
