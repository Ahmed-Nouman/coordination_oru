import csv
import os
from pathlib import Path
from collections import defaultdict
import numpy as np

base_directory = '../results/Baseline_4PV_4OP_MixedTraffic_BatteryBay_asynchronous_4/'

# Dictionary to hold the grouped folders
folder_groups = defaultdict(list)

# Traverse and group folders
for subdir, _, _ in os.walk(base_directory):
    if subdir != base_directory:
        main_folder_name = os.path.basename(subdir).rsplit('_', 1)[0]
        folder_groups[main_folder_name].append(subdir)

# Output file to combine all runs
output_filename = 'Combined_Run_Output.csv'
output_path = Path(base_directory) / output_filename
if output_path.exists():
    output_path.unlink()  # Remove the file if it already exists

# Process each folder independently as a "Run"
with open(output_path, 'w', newline='') as outfile:
    writer = csv.writer(outfile)

    # Loop through each folder as an individual run
    run_index = 1
    for main_name, folders in folder_groups.items():
        for folder in folders:
            output_variables_path = Path(folder) / 'OutputVariables.csv'
            if output_variables_path.exists():
                # Header for the run
                writer.writerow([f"RUN {run_index}"])
                writer.writerow([])
                writer.writerow(["Vehicle #", "Production Cycles", "Active Time", "Number of Stops"])

                # Process the data in the current OutputVariables.csv
                with open(output_variables_path, 'r') as infile:
                    reader = csv.DictReader(infile)
                    for row in reader:
                        robot_name = row.get("Robot Name")
                        if robot_name and robot_name.startswith("Vehicle_"):
                            writer.writerow([
                                robot_name,
                                row.get("Production Cycles", "0"),
                                row.get("Active Time (minutes)", "0"),
                                row.get("Number of Stops", "0")
                            ])

                # Increment run index and add blank lines for separation
                run_index += 1
                writer.writerow([])
                writer.writerow([])

print(f"All data has been processed and saved to {output_path} with separate run headers.")

