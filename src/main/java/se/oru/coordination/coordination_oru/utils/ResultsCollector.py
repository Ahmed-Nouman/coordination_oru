import os
import pandas as pd

root_directory = '../results/MixedTraffic_2PV_3SV_2OP_Closest/'

def read_robot_csv(filepath):
    if not os.path.exists(filepath):
        return None
    return pd.read_csv(filepath, delimiter=';')

def count_production_cycles(df):
    if df.empty:
        return 0

    path_indices = df['PathIndex'].tolist()
    count = 0
    for i in range(1, len(path_indices)):
        if path_indices[i] == -1 and path_indices[i-1] >= 0:
            count += 1
    return count

def get_time_spent(df):
    if df.empty:
        return 0

    pose_x = df['Pose_X'].tolist()
    pose_y = df['Pose_Y'].tolist()
    pose_theta = df['Pose_Theta'].tolist()

    active_periods = 0

    # Iterate over the DataFrame and check for changes in Pose_X, Pose_Y, or Pose_Theta
    for i in range(1, len(pose_x)):
        if pose_x[i] != pose_x[i-1] or pose_y[i] != pose_y[i-1] or pose_theta[i] != pose_theta[i-1]:
            active_periods += 1

    # Each row represents 0.10 seconds
    total_active_time = active_periods * 0.10 / 60  # Convert to minutes
    return total_active_time / 2  # Divide by 2

def get_coordination_time(df):
    if df.empty:
        return 0

    # Filter rows where CriticalPoint is non-negative
    coordination_df = df[df['CriticalPoint'] >= 0]

    # Calculate coordination time based on the number of rows and the fact that each row is 0.10 seconds apart
    total_coordination_time = len(coordination_df) * 0.10 / 60  # Convert to minutes
    return total_coordination_time

def count_stops(df):
    if df.empty:
        return 0

    pose_x = df['Pose_X'].tolist()
    pose_y = df['Pose_Y'].tolist()
    pose_theta = df['Pose_Theta'].tolist()

    stops = 0
    is_moving = False  # Tracks whether the vehicle was moving in the previous row
    stationary_count = 0  # Counts consecutive stationary rows

    # Iterate over the DataFrame and check if the vehicle has stopped moving
    for i in range(1, len(pose_x)):
        # Check if the vehicle is stationary in the current row
        is_stationary = (pose_x[i] == pose_x[i-1] and 
                         pose_y[i] == pose_y[i-1] and 
                         pose_theta[i] == pose_theta[i-1])

        # If the vehicle is stationary, increment the stationary counter
        if is_stationary:
            stationary_count += 1
        else:
            # If the vehicle is moving, reset the stationary count
            stationary_count = 0
            is_moving = True  # Vehicle is moving now

        # If the vehicle has been stationary for 5 consecutive rows, count it as a stop
        if stationary_count == 5 and is_moving:
            stops += 1
            is_moving = False  # Reset moving flag since the vehicle is stationary
            stationary_count = 0  # Reset the stationary count after detecting a stop

    return stops

# Function to extract the numeric part of the vehicle names
def extract_vehicle_number(vehicle_name):
    return int(vehicle_name.split('_')[1])

if __name__ == "__main__":
    for dirpath, _, filenames in os.walk(root_directory):
        robot_files = sorted([f for f in filenames if f.startswith("Vehicle_") and f.endswith(".csv")])

        if not robot_files:
            continue

        results = {'Robot Name': [], 'Production Cycles': [], 'Active Time (minutes)': [], 'Number of Stops': []}

        for file in robot_files:
            robot_name = file[:-4]
            filepath = os.path.join(dirpath, file)
            df = read_robot_csv(filepath)

            if df is not None:
                results['Robot Name'].append(robot_name)

                # Production Cycles
                cycles_count = count_production_cycles(df)
                results['Production Cycles'].append(cycles_count)

                # Active Robot Time (divided by 2)
                robot_time_spent = get_time_spent(df)
                results['Active Time (minutes)'].append(f"{robot_time_spent:.2f}")

                # Number of Stops (subtract production cycles)
                stops_count = count_stops(df) - cycles_count
                results['Number of Stops'].append(stops_count)

        # Create DataFrame from results
        results_df = pd.DataFrame(results)

        # Sort the DataFrame by extracting the numeric part of 'Robot Name'
        results_df['Vehicle_Number'] = results_df['Robot Name'].apply(extract_vehicle_number)
        results_df = results_df.sort_values(by='Vehicle_Number').drop(columns='Vehicle_Number')

        # Write to CSV file
        output_csv_path = os.path.join(dirpath, 'OutputVariables.csv')
        results_df.to_csv(output_csv_path, index=False)

    print("OutputVariables.csv files written to all subfolders.")

