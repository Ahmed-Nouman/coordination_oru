import csv
import os
from pathlib import Path
from collections import defaultdict
import numpy as np

base_directory = '../results/'

# Dictionary to hold the grouped folders
folder_groups = defaultdict(list)

# Traverse and group folders
for scenario_dir in os.listdir(base_directory):
    scenario_path = os.path.join(base_directory, scenario_dir)

    # Skip if it's not a directory
    if not os.path.isdir(scenario_path):
        continue

    for subdir, _, _ in os.walk(scenario_path):
        if subdir != scenario_path:
            main_folder_name = os.path.basename(subdir).rsplit('_', 1)[0]
            folder_groups[main_folder_name].append(subdir)

# Process each group
for main_name, folders in folder_groups.items():
    fieldnames_set = set()
    all_data = []
    robot_highest_data = []
    numeric_data = defaultdict(list)
    numeric_data_highest = defaultdict(list)
    highest_robot_number = -1

    # Process each folder in the group
    for folder in folders:
        output_variables_path = Path(folder) / 'OutputVariables.csv'
        if output_variables_path.exists():
            with open(output_variables_path, 'r') as infile:
                reader = csv.DictReader(infile)
                fieldnames_set.update(reader.fieldnames)
                for row in reader:
                    robot_name = row.get('Robot Name')
                    if robot_name and robot_name.startswith('Vehicle_'):
                        try:
                            robot_number = int(robot_name.split('_')[1])
                            if robot_number > highest_robot_number:
                                highest_robot_number = robot_number
                        except ValueError:
                            continue  # Ignore if robot_number is not an integer

    highest_robot_name = f'Vehicle_{highest_robot_number}'

    for folder in folders:
        output_variables_path = Path(folder) / 'OutputVariables.csv'
        if output_variables_path.exists():
            with open(output_variables_path, 'r') as infile:
                reader = csv.DictReader(infile)
                for row in reader:
                    if row.get('Robot Name') == highest_robot_name:
                        robot_highest_data.append(row)
                        for key, value in row.items():
                            if key != 'Robot Name':  # Exclude 'Robot Name' from numeric processing
                                try:
                                    numeric_data_highest[key].append(float(value))
                                except ValueError:
                                    continue  # Ignore non-numeric data
                    else:
                        all_data.append(row)
                        for key, value in row.items():
                            if key != 'Robot Name':  # Exclude 'Robot Name' from numeric processing
                                try:
                                    numeric_data[key].append(float(value))
                                except ValueError:
                                    continue  # Ignore non-numeric data

        # Generate fieldnames for mean and standard deviation
        numeric_keys = {key for key in fieldnames_set if key != 'Robot Name'}
        stat_fieldnames = [f"{key}_{stat}" for key in numeric_keys for stat in ['mean', 'stddev']]
        fieldnames = sorted(fieldnames_set, reverse=True) + sorted(stat_fieldnames, reverse=False)

        # Output file path specific to the current subdirectory with the name "output.csv"
        output_filename = 'output.csv'
        output_path = Path(folder) / output_filename
        if output_path.exists():
            output_path.unlink()  # Remove the file if it already exists

        # Write combined data to the output file in the respective subdirectory
        with open(output_path, 'w', newline='') as outfile:
            writer = csv.DictWriter(outfile, fieldnames=fieldnames)
            writer.writeheader()

            # Write all data except the highest Robot number
            for row in all_data:
                writer.writerow(row)

            # Leave one blank line for clear separation
            writer.writerow({})

            # Calculate and write statistics for other robots
            stats = {}
            for key, values in numeric_data.items():
                if values:  # Ensure there's data to calculate statistics
                    stats[f'{key}_mean'] = round(np.mean(values), 2)
                    stats[f'{key}_stddev'] = round(np.std(values), 2)
            writer.writerow(stats)

            # Leave one blank line for clear separation
            writer.writerow({})

            # Write data for the highest Robot number
            for row in robot_highest_data:
                writer.writerow(row)

            # Leave one blank line before statistics
            writer.writerow({})

            # Calculate and write statistics for the highest Robot number
            stats_highest = {}
            for key, values in numeric_data_highest.items():
                if values:
                    stats_highest[f'{key}_mean'] = round(np.mean(values), 2)
                    stats_highest[f'{key}_stddev'] = round(np.std(values), 2)
            writer.writerow(stats_highest)

        print(f"Data from OutputVariables.csv combined, statistics calculated, and saved to {output_path}")

