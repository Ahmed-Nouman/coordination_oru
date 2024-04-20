import os
import pandas as pd

root_directory = '../results/HeuristicsPaperScenario/'

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

    # Filter rows where PathIndex is positive
    active_df = df[df['Velocity'] >= 0]
    
    # Calculate active time based on the number of rows and the fact that each row is 0.10 seconds apart
    total_active_time = len(active_df) * 0.10 / 60  # Convert to minutes
    return total_active_time

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
    
    velocity = df['Velocity'].tolist()
    stops = 0
    for i in range(1, len(velocity)):
        if velocity[i] <= 0.0 and velocity[i-1] > 0.0:
            stops += 1
    return stops

if __name__ == "__main__":
    for dirpath, _, filenames in os.walk(root_directory):
        robot_files = sorted([f for f in filenames if f.startswith("Robot_") and f.endswith(".csv")])

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

                # Active Robot Time
                robot_time_spent = get_time_spent(df)
                results['Active Time (minutes)'].append(f"{robot_time_spent:.2f}")

                # Number of Stops
                stops_count = count_stops(df)
                results['Number of Stops'].append(stops_count)

                # Active Coordination time
                # coordination_time = get_coordination_time(df)
                # results['Coordination Time (minutes)'].append(f"{coordination_time:.2f}")
        
        # Create DataFrame from results
        results_df = pd.DataFrame(results)

        # Write to CSV file
        output_csv_path = os.path.join(dirpath, 'OutputVariables.csv')
        results_df.to_csv(output_csv_path, index=False)

    print("OutputVariables.csv files written to all subfolders.")
