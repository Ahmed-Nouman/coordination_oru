import os
from shutil import copy2
from pathlib import Path

# Define the base directory where folders are scanned
base_directory = '../results/HeuristicsPaperScenario/'

# Define the destination directory for the results
results_directory = Path('/home/ra2/results')
results_directory.mkdir(exist_ok=True)  # Create the results directory if it does not exist

# Function to generate a new file name if the file already exists
def generate_new_filename(original_path):
    base = original_path.stem
    suffix = original_path.suffix
    counter = 1
    new_path = original_path
    while new_path.exists():
        new_path = original_path.with_name(f"{base}({counter}){suffix}")
        counter += 1
    return new_path

# Function to process folders and files
def process_folders(base_dir, results_dir):
    processed_files = set()  # Track files that have been processed

    # Scan through all items in the base directory
    for item in os.listdir(base_dir):
        full_path = os.path.join(base_dir, item)
        if os.path.isdir(full_path):  # Ensure it's a directory
            # Check if the folder name fits the expected pattern
            if '_' in item and item.count('_') >= 5:
                # Remove the timestamp from the folder name
                name_parts = item.split('_')
                cleaned_name = '_'.join(name_parts[:4])  # Assume first four parts are the actual name

                # Search for a file with the cleaned name inside this folder
                for file in os.listdir(full_path):
                    if file.startswith(cleaned_name) and file not in processed_files:
                        source_file = os.path.join(full_path, file)
                        # Ensure the found item is actually a file before copying
                        if os.path.isfile(source_file):
                            destination_file = Path(results_dir, file)
                            # Check if the file already exists and change the name accordingly
                            if destination_file.exists():
                                destination_file = generate_new_filename(destination_file)
                            copy2(source_file, destination_file)  # Copy the file to the results directory
                            print(f"Copied {file} to {destination_file}")
                            processed_files.add(file)
                        else:
                            print(f"Skipped {source_file} because it is not a file.")

# Run the processing function
process_folders(base_directory, results_directory)

