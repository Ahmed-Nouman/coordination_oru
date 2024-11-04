#!/bin/bash

# Number of repetitions
REPEAT=3
# Duration to keep IntelliJ open (50 minutes in seconds)
DURATION=3000

for i in $(seq 1 $REPEAT); do
    echo "Starting IntelliJ Community Edition - Attempt $i"

    # Start IntelliJ Community Edition
    intellij-idea-community &  # Adjust this command if needed
    IDEA_PID=$!  # Capture the process ID of IntelliJ
    sleep 30  # Wait for IntelliJ to load completely; adjust as necessary
    
    # Use wmctrl to focus IntelliJ window
    wmctrl -a "IntelliJ IDEA"
    sleep 2

    # Use xdotool to send Control+Shift+F10 to run the current file
    xdotool keydown Control keydown Shift
    sleep 0.1
    xdotool key F10
    sleep 0.1
    xdotool keyup Control keyup Shift
    echo "Sent Control+Shift+F10 to run the current file"
    sleep 1
    
    # Wait for the specified duration (50 minutes)
    sleep $DURATION

    # Terminate IntelliJ process directly
    echo "Closing IntelliJ - Attempt $i"
    kill $IDEA_PID

    # Ensure IntelliJ closes completely
    while pgrep -x "intellij-idea-community" > /dev/null; do
        echo "Waiting for IntelliJ to close..."
        sleep 2
    done
    
    echo "IntelliJ closed - Completed Attempt $i"
    
    # Pause before the next attempt
    sleep 10
done

