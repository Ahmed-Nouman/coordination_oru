import csv
import os
from pathlib import Path
import math

base_directory = '../results/lookAheadPaper_2023/'
output_filename = 'ReportsMerged.csv'

def compute_distance(x1, y1, x2, y2):
    return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)

for subdir, _, files in os.walk(base_directory):
    if subdir == base_directory:
        continue

    robot_positions = {}
    robot_files = [f for f in files if f.startswith('Robot_') and f.endswith('.csv')]
    robot_numbers = sorted([int(f.split('_')[1].split('.')[0]) for f in robot_files])
    
    highest_robot_num = max(robot_numbers)

    output_path = Path(subdir) / output_filename
    if output_path.exists():
        output_path.unlink()  # Remove the file if it already exists

    with open(output_path, 'w', newline='') as outfile:
        # Determine the CSV fieldnames
        fieldnames = ['Time(s)']
        for metric in ['V', 'D', 'CP']:
            for num in robot_numbers:
                fieldnames.append(f'{metric}_{num}(m/s)' if metric == 'V' else f'{metric}_{num}')
        for num in robot_numbers:
            if num != highest_robot_num:
                fieldnames.append(f'DH_{num}(m)')

        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()

        all_rows = []

        # Read data from the robot CSV files
        for file in robot_files:
            input_path = Path(subdir) / file
            with open(input_path, 'r') as infile:
                reader = csv.DictReader(infile, delimiter=';')
                robot_num = file.split('_')[1].split('.')[0]

                for idx, row in enumerate(reader):
                    if idx >= len(all_rows):
                        all_rows.append({'Time(s)': idx * 0.25})
                    
                    all_rows[idx].update({
                        f'V_{robot_num}(m/s)': round(float(row['Velocity']), 1),
                        f'D_{robot_num}': round(float(row['DistanceTraveled']), 1),
                        f'CP_{robot_num}': round(float(row['CriticalPoint']), 1),
                    })
                    
                    # Calculate distance from highest robot for this time step
                    if int(robot_num) != highest_robot_num:
                        highest_robot_position = (
                            float(all_rows[idx].get(f'Pose_X_{highest_robot_num}', 0)),
                            float(all_rows[idx].get(f'Pose_Y_{highest_robot_num}', 0))
                        )
                        current_robot_position = (float(row['Pose_X']), float(row['Pose_Y']))
                        distance = compute_distance(
                            current_robot_position[0], current_robot_position[1],
                            highest_robot_position[0], highest_robot_position[1]
                        )
                        all_rows[idx][f'DH_{robot_num}(m)'] = round(distance, 1)

        writer.writerows(all_rows)

    print(f"All data in {subdir} combined. Output saved to {output_path}.")

