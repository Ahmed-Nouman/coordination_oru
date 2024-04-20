import csv
import os
from pathlib import Path
from collections import defaultdict
import numpy as np

base_directory = '../results/HeuristicsPaperScenario/'

# Dictionary to hold the grouped folders
folder_groups = defaultdict(list)

# Traverse and group folders
for subdir, _, _ in os.walk(base_directory):
    if subdir != base_directory:
        main_folder_name = os.path.basename(subdir).rsplit('_', 1)[0]
        folder_groups[main_folder_name].append(subdir)

# Process each group
for main_name, folders in folder_groups.items():
    output_filename = f'{main_name}.csv'
    output_path = Path(base_directory) / output_filename
    if output_path.exists():
        output_path.unlink()  # Remove the file if it already exists

    fieldnames_set = set()
    all_data = []
    robot_5_data = []
    numeric_data = defaultdict(list)
    numeric_data_5 = defaultdict(list)

    # Process each folder in the group
    for folder in folders:
        output_variables_path = Path(folder) / 'OutputVariables.csv'
        if output_variables_path.exists():
            with open(output_variables_path, 'r') as infile:
                reader = csv.DictReader(infile)
                fieldnames_set.update(reader.fieldnames)
                for row in reader:
                    if row.get('Robot Name') == 'Robot_5':
                        robot_5_data.append(row)
                        for key, value in row.items():
                            if key != 'Robot Name':  # Exclude 'Robot Name' from numeric processing
                                try:
                                    numeric_data_5[key].append(float(value))
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
    fieldnames = sorted(fieldnames_set, reverse=True) + stat_fieldnames

    # Write combined data to output file
    with open(output_path, 'w', newline='') as outfile:
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()

        # Write all data except Robot_5
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

        # Write Robot_5 data
        for row in robot_5_data:
            writer.writerow(row)

        # Leave one blank line before statistics
        writer.writerow({})

        # Calculate and write statistics for Robot_5
        stats_5 = {}
        for key, values in numeric_data_5.items():
            if values:
                stats_5[f'{key}_mean'] = round(np.mean(values), 2)
                stats_5[f'{key}_stddev'] = round(np.std(values), 2)
        writer.writerow(stats_5)

    print(f"Data from OutputVariables.csv combined, statistics calculated, and everything saved to {output_path}")

