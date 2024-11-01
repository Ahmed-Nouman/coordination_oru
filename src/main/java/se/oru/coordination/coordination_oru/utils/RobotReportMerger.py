import csv
import math
import os
from pathlib import Path

base_directory = '../results/MixedTraffic_8PV_6SV_2OP_Closest/'

def compute_distance(x1, y1, x2, y2):
    return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)

# Function to process each directory
def process_directory(subdir, reference_vehicles):
    robot_files = [f for f in os.listdir(subdir) if f.startswith('Vehicle_') and f.endswith('.csv')]
    robot_numbers = sorted([int(f.split('_')[1].split('.')[0]) for f in robot_files])
    
    # Change the output file name to DataMerged.csv
    output_filename = 'DataMerged.csv'
    output_path = Path(subdir) / output_filename
    if output_path.exists():
        output_path.unlink()  # Remove the file if it already exists

    with open(output_path, 'w', newline='') as outfile:
        fieldnames = ['Time(s)']
        for metric in ['V', 'D']:
            for num in robot_numbers:
                fieldnames.append(f'{metric}_{num}(m/s)' if metric == 'V' else f'{metric}_{num}')

        for ref_vehicle in reference_vehicles:
            for num in robot_numbers:
                if num != ref_vehicle:
                    fieldnames.append(f'D_{ref_vehicle}_to_{num}(m)')

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

        # Calculate D_i for each row
        for idx, row in enumerate(all_rows):
            for ref_vehicle in reference_vehicles:
                if idx < len(robot_positions[ref_vehicle]):
                    position_ref = robot_positions[ref_vehicle][idx]
                    for num in robot_numbers:
                        if num == ref_vehicle or idx >= len(robot_positions[num]):
                            continue  # Skip if it's the reference vehicle itself or out of range
                        position_i = robot_positions[num][idx]
                        distance = compute_distance(position_i[0], position_i[1], position_ref[0], position_ref[1])
                        row[f'D_{ref_vehicle}_to_{num}(m)'] = round(10 * distance, 1)

        writer.writerows(all_rows)

    print(f"All data in {subdir} combined. Output saved to {output_path}.")

# Input reference vehicles
reference_vehicles = [2, 4]  # You can change this to any other vehicles you want to compare

# Walk through directories and process each
for subdir, _, files in os.walk(base_directory):
    if subdir == base_directory:
        continue
    process_directory(subdir, reference_vehicles)

