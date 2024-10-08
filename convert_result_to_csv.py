#!/usr/bin/env python3

import os
import csv
import json
from datetime import datetime
import subprocess
import argparse

__location__ = os.path.realpath(
    os.path.join(os.getcwd(), os.path.dirname(__file__)))


def parse_arguments():
    parser = argparse.ArgumentParser(description="Summarize benchmark results to csv format. "
                                                 "Columns are 'Bench', 'Query', 'Time', 'Token_remaining' respectively")
    parser.add_argument("-d", "--input-directory", default=None, type=str, nargs='+',
                        help="Input directory containing the benchmark results in the type of '*.json'.")
    parser.add_argument("-o", "--output-path", default=None, type=str,
                        help="path to save the result.")
    parser.add_argument("-c", "--comparison-mode", action="store_true",
                        help="Extra parameters/columns will be exported for comparison between settings. "
                             "Suitable when benchmark results in the input-directory are under different settings.")
    return parser.parse_args()


def get_file_list(directory: str) -> list:
    """Return a list of benchmark json output files within the specified directory"""
    return [os.path.join(directory, x) for x in os.listdir(directory) if x.lower().endswith('.json')]


def extract_from_json(filename: str, header: list) -> list:
    """Return a list of entries specified by input param 'header' from the given file 'filename'"""
    with open(filename, 'r') as file:
        data = json.load(file)
    return [data[entry] for entry in header]


def csv_filename_generator(flag: str) -> str:
    """Generate a proper name for the folder"""
    time = datetime.now().strftime("%Y%m%d-%H%M%S")
    git_head_id = subprocess.check_output(
        ['git', 'rev-parse', '--short', 'HEAD'],
        stderr=subprocess.STDOUT
    )
    git_head_id = git_head_id.decode("utf-8").strip()

    if flag:
        return f'benchmark_csv_{time}_{git_head_id}_{flag}'
    else:
        return f'benchmark_csv_{time}_{git_head_id}'


def write_to_csv(output_path: str, rowlist: list):
    """Write all rows in 'rowlist' to 'output_path'"""
    with open(output_path, 'w') as file:
        writer = csv.writer(file)
        writer.writerows(rowlist)

def output_manager(output_path: str, rowlist: list):
    """Manage sub-directory creation and output file naming"""
    directory = os.path.dirname(output_path)
    if directory and not os.path.exists(directory):
        os.makedirs(directory)
    write_to_csv(output_path, rowlist)


def main():
    arguments = parse_arguments()
    folders = arguments.input_directory
    output_path = arguments.output_path
    comparison_mode = arguments.comparison_mode

    file_list = []
    for folder in folders:
        benchmark_results_location = os.path.abspath(folder)
        file_list.extend(get_file_list(benchmark_results_location))

    if comparison_mode:
        header_row = ['Subject', 'Query', 'Time', 'Token_remaining', 'Environment']
    else:
        header_row = ['Subject', 'Query', 'Time', 'Token_remaining']

    csv_rowlist = [header_row]
    for file in file_list:
        print(os.path.basename(file))
        csv_row = extract_from_json(file, header_row)
        csv_rowlist.append(csv_row)

    if comparison_mode:
        csv_rowlist.sort(key=lambda x: (x[0], x[4], x[2], x[1]))
    else:
        csv_rowlist.sort(key=lambda x: (x[0], x[2], x[1]))

    if output_path:
        output_manager(output_path, csv_rowlist)

    for row in csv_rowlist:
        print(', '.join(map(lambda x: str(x), row)))

if __name__ == "__main__":
    main()
