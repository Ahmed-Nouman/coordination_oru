import csv
import math
import os
from pathlib import Path

base_directory = '../results/productionCycleDrilling/'

def compute_distance(x1, y1, x2, y2):
    return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)

for subdir, _, files in os.walk(base_directory):
    if subdir == base_directory:
        continue

    robot_files = [f for f in files if f.startswith('Robot_') and f.endswith('.csv')]
    robot_numbers = sorted([int(f.split('_')[1].split('.')[0]) for f in robot_files])
    highest_robot_num = max(robot_numbers)

    folder_name = os.path.basename(subdir).rsplit('_', 1)[0]
    output_filename = f'{folder_name}.csv'
    output_path = Path(subdir) / output_filename
    if output_path.exists():
        output_path.unlink()  # Remove the file if it already exists

    with open(output_path, 'w', newline='') as outfile:
        fieldnames = ['Time(s)']
        for metric in ['V', 'D']:
            for num in robot_numbers:
                fieldnames.append(f'{metric}_{num}(m/s)' if metric == 'V' else f'{metric}_{num}')
        
        for i in range(1, highest_robot_num):
            fieldnames.append(f'DS_{i}(m)')

        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()

        all_rows = []

        robot_positions = {num: [] for num in robot_numbers}
        for file in robot_files:
            input_path = Path(subdir) / file
            robot_num = int(file.split('_')[1].split('.')[0])
            with open(input_path, 'r') as infile:
                reader = csv.DictReader(infile, delimiter=';')
                for idx, row in enumerate(reader):
                    if idx >= len(all_rows):
                        all_rows.append({'Time(s)': idx * 0.1})
                    all_rows[idx].update({
                        f'V_{robot_num}(m/s)': round(float(row['Velocity']), 1),
                        f'D_{robot_num}': round(float(row['DistanceTraveled']), 1),
                    })
                    robot_positions[robot_num].append((float(row['Pose_X']), float(row['Pose_Y'])))

        # Calculate DS_i for each row
        for idx, row in enumerate(all_rows):
            position_last = robot_positions[highest_robot_num][idx] if idx < len(robot_positions[highest_robot_num]) else None
            for i in range(1, highest_robot_num):
                if position_last is None or idx >= len(robot_positions[i]):
                    continue  # Skip the calculation if the index is out of range for the last robot or any other robot.
                position_i = robot_positions[i][idx]
                distance = compute_distance(position_i[0], position_i[1], position_last[0], position_last[1])
                row[f'DS_{i}(m)'] = round(10 * distance, 1)

        writer.writerows(all_rows)

    print(f"All data in {subdir} combined. Output saved to {output_path}.")

