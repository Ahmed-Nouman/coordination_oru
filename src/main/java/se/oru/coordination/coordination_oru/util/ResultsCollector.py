import os
import pandas as pd

root_directory = '../results/lookAheadPaper_2023/'

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
    active_df = df[df['PathIndex'] >= 0]
    
    # Calculate active time based on the number of rows and the fact that each row is 0.25 seconds apart
    total_active_time = len(active_df) * 0.25 / 60  # Convert to minutes
    return total_active_time

def get_coordination_time(df):
    if df.empty:
        return 0

    # Filter rows where CriticalPoint is non-negative
    coordination_df = df[df['CriticalPoint'] >= 0]

    # Calculate coordination time based on the number of rows and the fact that each row is 0.25 seconds apart
    total_coordination_time = len(coordination_df) * 0.25 / 60  # Convert to minutes
    return total_coordination_time

if __name__ == "__main__":
    for dirpath, _, filenames in os.walk(root_directory):
        robot_files = sorted([f for f in filenames if f.startswith("Robot_") and f.endswith(".csv")])

        if not robot_files:
            continue
        
        production_cycles_results = []
        active_time_results = []
        coordination_time_results = []

        for file in robot_files:
            robot_name = file[:-4]
            filepath = os.path.join(dirpath, file)
            df = read_robot_csv(filepath)
            
            if df is not None:
                # Production Cycles
                cycles_count = count_production_cycles(df)
                production_cycles_results.append(f"{robot_name} Production Cycles: {cycles_count}")

                # Active Robot Time
                robot_time_spent = get_time_spent(df)
                active_time_results.append(f"Active Time for {robot_name} = {robot_time_spent:.2f} minutes")

                # Active Coordination Time
                coordination_time = get_coordination_time(df)
                coordination_time_results.append(f"Active Coordination Time for {robot_name} = {coordination_time:.2f} minutes")
            
        # Consolidate results
        results = production_cycles_results + active_time_results + coordination_time_results

        # Write to file
        if results:
            with open(os.path.join(dirpath, 'OutputVariables.txt'), 'w') as f:
                for line in results:
                    f.write(f"{line}\n")

    print("OutputVariables.txt files written to all subfolders.")

