import subprocess
import time

# List of scripts you want to run
scripts = [
    "RobotReportMerger.py",
    "ResultsCollector.py",
    "ResultsCalculator.py"
]

# Time in seconds to wait between each script
delay_between_scripts = 1  # Delay of 1 second

# Arguments to pass to the scripts
arguments = ["2", "4"]

# Loop through each script and run them one by one with arguments
for script in scripts:
    print(f"Running {script} with arguments {arguments}...")
    subprocess.run(["python3", script] + arguments)
    print(f"{script} completed. Waiting for {delay_between_scripts} seconds...")
    time.sleep(delay_between_scripts)

print("All scripts have been executed.")

