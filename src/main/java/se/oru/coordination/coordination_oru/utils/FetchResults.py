import subprocess
import time

# List of scripts you want to run
scripts = [
    "RobotReportMerger.py",
    "ResultsCollector.py",
    "ResultsCalculator.py"
]

# Time in seconds to wait between each script
delay_between_scripts = 1  # Delay of 1 seconds

# Loop through each script and run them one by one
for script in scripts:
    print(f"Running {script}...")
    subprocess.run(["python3", script])
    print(f"{script} completed. Waiting for {delay_between_scripts} seconds...")
    time.sleep(delay_between_scripts)

print("All scripts have been executed.")

