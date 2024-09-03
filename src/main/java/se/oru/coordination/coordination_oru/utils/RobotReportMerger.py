import csv
import math
import os
from pathlib import Path
import argparse

def compute_distance(x1, y1, x2, y2):
    return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)

def process_files(scenario_path, vehicle_ids):
    for subdir, _, files in os.walk(scenario_path):
        if subdir == scenario_path:
            continue

        robot_files = [f for f in files if f.startswith('Vehicle_') and f.endswith('.csv')]
        if not robot_files:
            continue  # Skip if there are no robot files in the directory

        robot_numbers = sorted([int(f.split('_')[1].split('.')[0]) for f in robot_files])

        # Check if the specified vehicles exist in the data
        for vehicle_id in vehicle_ids:
            if vehicle_id not in robot_numbers:
                print(f"Vehicle {vehicle_id} does not exist in {subdir}. Skipping...")
                return

        # Set the output filename to 'DataMerged.csv'
        output_filename = 'DataMerged.csv'
        output_path = Path(subdir) / output_filename
        if output_path.exists():
            output_path.unlink()  # Remove the file if it already exists

        with open(output_path, 'w', newline='') as outfile:
            fieldnames = ['Time(s)']
            for metric in ['V', 'D']:
                for num in robot_numbers:
                    fieldnames.append(f'{metric}_{num}(m/s)' if metric == 'V' else f'{metric}_{num}')
            
            for vehicle_id in vehicle_ids:
                for i in robot_numbers:
                    if i != vehicle_id:
                        fieldnames.append(f'D_{vehicle_id}_{i}(m)')

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

            # Calculate D_i for each row for specified vehicle IDs
            for idx, row in enumerate(all_rows):
                for vehicle_id in vehicle_ids:
                    if idx < len(robot_positions[vehicle_id]):
                        position_vehicle = robot_positions[vehicle_id][idx]
                        for i in robot_numbers:
                            if i != vehicle_id and idx < len(robot_positions[i]):
                                position_i = robot_positions[i][idx]
                                distance = compute_distance(position_vehicle[0], position_vehicle[1], position_i[0], position_i[1])
                                row[f'D_{vehicle_id}_{i}(m)'] = round(10 * distance, 1)

            writer.writerows(all_rows)

        print(f"All data in {subdir} combined. Output saved to {output_path}.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process and calculate distances between specified vehicles.")
    parser.add_argument(
        "vehicles",
        metavar="N",
        type=int,
        nargs="+",
        help="List of vehicle numbers to calculate distances for"
    )

    args = parser.parse_args()

    base_directory = '../results/'

    for scenario_dir in os.listdir(base_directory):
        scenario_path = Path(base_directory) / scenario_dir

        # Skip if it's not a directory
        if not scenario_path.is_dir():
            continue

        process_files(scenario_path, args.vehicles)

