import os
import sys
import argparse
import csv
import re
from collections import Counter
import re
import numpy as np
import pandas as pd
from scipy.stats import kendalltau, spearmanr

def get_data_file(directory):
    files = [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]
    txt_files = [f for f in files if f.endswith('.txt')]
    if len(txt_files) != 1:
        raise ValueError(f"Error: There should be exactly one txt file in the directory {directory}, found {len(txt_files)}")
    return os.path.join(directory, txt_files[0])


# =============================================== For Time and Number of Queries ================================================== #

def get_global_time_and_queries(data_file_path):
    # Read and process the content of the given txt file
    total_tests = 0
    total_time = 0
    with open(data_file_path, 'r') as file:
        content = file.read()
        sections = content.split('***********************************************************************************************************************************************')
        for section in sections:
            lines = section.strip().split('\n')
            for line in lines:
                if line.startswith('Number of tests:'):
                    total_tests += int(line.split(':')[-1].strip())
                elif line.startswith('Time:'):
                    total_time += int(line.split(':')[-1].strip().replace(' ms', ''))
                    
    return total_tests, total_time

def get_global_info_all_benchmarks(data_dir, output_csv):
    with open(output_csv, 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Benchmark', 'Total Tests', 'Total Time (ms)'])
        
        directories = [d for d in os.listdir(data_dir) if os.path.isdir(os.path.join(data_dir, d))]
        
        for directory in directories:
            dir_path = os.path.join(data_dir, directory)
            txt_file_path = get_data_file(dir_path)
            total_tests, total_time = get_global_time_and_queries(txt_file_path)
            csv_writer.writerow([directory, total_tests, total_time])


# =============================================== For Tokens ================================================== #

def get_global_token_info(data_file_path):
    tokens_before = 0
    tokens_after = 0
    with open(data_file_path, 'r') as file:
        content = file.read()
        sections = content.split('***********************************************************************************************************************************************')
        for section in sections:
            lines = section.strip().split('\n')
            for line in lines:
                if line.startswith('Sum of weight before reduction'):
                    tokens_before += int(line.split(':')[-1].strip())
                elif line.startswith('Sum of weight after reduction'):
                    tokens_after += int(line.split(':')[-1].strip())

    return tokens_before, tokens_after

def get_global_token_info_all_benchmarks(data_dir, output_csv):
    with open(output_csv, 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Benchmark', 'Tokens Before', 'Tokens After', 'Tokens Reduction'])
        
        directories = [d for d in os.listdir(data_dir) if os.path.isdir(os.path.join(data_dir, d))]
        
        for directory in directories:
            dir_path = os.path.join(data_dir, directory)
            txt_file_path = get_data_file(dir_path)
            tokens_before, tokens_after = get_global_token_info(txt_file_path)
            tokens_reduction = tokens_before - tokens_after
            csv_writer.writerow([directory, tokens_before, tokens_after, tokens_reduction])

# def process_wdd_file(txt_file_path):
#     # Read and process the content of the given txt file
#     weight_counts_before = Counter()
#     weight_counts_after = Counter()
#     with open(txt_file_path, 'r') as file:
#         content = file.read()
#         sections = content.split('***********************************************************************************************************************************************')
#         for section in sections:
#             # Extract and count weights before reduction
#             match_before = re.search(r'weight_list\(before reduction\):\s+\[(.*?)\]', section)
#             if match_before:
#                 weights_before = [int(x.strip()) for x in match_before.group(1).split(',') if x.strip()]
#                 weight_counts_before.update(weights_before)
            
#             # Extract and count weights after reduction
#             match_after = re.search(r'weight_list\(after reduction\):\s+\[(.*?)\]', section)
#             if match_after:
#                 weights_after = [int(x.strip()) for x in match_after.group(1).split(',') if x.strip()]
#                 weight_counts_after.update(weights_after)

#     # print(weight_counts_before)
#     # print(weight_counts_after)
#     return weight_counts_before, weight_counts_after

# =============================================== For Average Weight ================================================== # 

def get_average_weight_info(file):
    average_weight_list = []
    with open(file, 'r') as f:
        content = f.read()
        sections = content.split('***********************************************************************************************************************************************')
        for section in sections:
            before = 0
            after = 0
            lines = section.strip().split('\n')
            for line in lines:
                if line.startswith('Average weight of best before reduction') or line.startswith('Average weight before reduction'):
                    before = float(line.split(':')[-1].strip())
                elif line.startswith('Average weight of best after reduction') or line.startswith('Average weight after reduction'):
                    after = float(line.split(':')[-1].strip())
            if before > 0 and after > 0:
                average_weight_list.append((before, after))
    return average_weight_list

def get_weight_list(file):
    weight_list = []
    with open(file, 'r') as f:
        content = f.read()
        sections = content.split('***********************************************************************************************************************************************')
        for section in sections:
            before = []
            after = []
            lines = section.strip().split('\n')
            for line in lines:
                # eg: weight_list(before reduction): [5, 5, 5, 4, 6, 5, 6, 5, 7, 14, ]
                if line.startswith('weight_list(before reduction):'):
                    # remove the first and last characters '[' and ']'
                    before = [int(x) for x in line.split(':')[-1].strip()[1:-1].split(', ') if x]
                    # before = [int(x) for x in line.split(':')[-1].strip().split(', ') if x]
                elif line.startswith('weight_list(after reduction):'):
                    after = [int(x) for x in line.split(':')[-1].strip()[1:-1].split(', ') if x]
                    # after = [int(x) for x in line.split(':')[-1].strip().split(', ') if x]
            if before and after:
                weight_list.append((before, after))
    return weight_list

def get_input_length(file):
    input_length_list = []
    with open(file, 'r') as f:
        content = f.read()
        sections = content.split('***********************************************************************************************************************************************')
        for section in sections:
            before = []
            after = []
            lines = section.strip().split('\n')
            for line in lines:
                # eg: [Length of the list before reduction: 7]
                if line.startswith('[Length of the list before reduction:'):
                    # print(line.strip()[1:-1].split(':')[-1].strip())
                    before = int(line.strip()[1:-1].split(':')[-1].strip())
                elif line.startswith('[Length of the list after reduction:'):
                    after = int(line.strip()[1:-1].split(':')[-1].strip())
            if before and after:
                input_length_list.append((before, after))
    return input_length_list

def stat_weight_info(base_directory, output_csv):
    directories = [d for d in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, d))]
    with open(output_csv, 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Benchmark', 'Average Weight Before', 'Average Weight After', 'diff', 'ratio'])
        for directory in directories:
            dir_path = os.path.join(base_directory, directory)
            file = get_data_file(dir_path)
            average_weight_list = get_average_weight_info(file)
            # write to csv
            for before, after in average_weight_list:
                diff = after - before
                ratio = after / before
                csv_writer.writerow([directory, before, after, diff, ratio])

def stat_correlation(base_directory, output_csv):
    directories = [d for d in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, d))]
    with open(output_csv, 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Benchmark', 'len(list_before_reduction)', 'len(list_after_reduction)', 'kendall_corr', 'kendall_p_value', 'spearman_corr', 'spearman_p_value'])
        for directory in directories:
            print(f"Processing {directory}")
            dir_path = os.path.join(base_directory, directory)
            file = get_data_file(dir_path)
            weight_list = get_weight_list(file)
            for before_reduction, after_reduction in weight_list:
                before_reduction_unique = list(set(before_reduction))
                before_reduction_unique.sort()

                # calculate correlation
                all_values = []
                all_labels = []
                for elem in before_reduction_unique:
                    all_values.append(elem)
                    cnt_before = before_reduction.count(elem)
                    cnt_after = after_reduction.count(elem)
                    all_labels.append((cnt_before-cnt_after)/cnt_before)

                df = pd.DataFrame({'value': all_values, 'deleted': all_labels})

                kendall_corr, kendall_p_value = kendalltau(df['value'], df['deleted'])
                # print(f"Kendall's Tau: {kendall_corr}, p-value: {kendall_p_value}")

                spearman_corr, spearman_p_value = spearmanr(df['value'], df['deleted'])
                # print(f"Spearman's Rank Correlation: {spearman_corr}, p-value: {spearman_p_value}")
                # filter out the nan values
                # if not np.isnan(kendall_corr) or not np.isnan(spearman_corr):
                csv_writer.writerow([directory, len(before_reduction), len(after_reduction), kendall_corr, kendall_p_value, spearman_corr, spearman_p_value])
                
        # flush the csv file
        csvfile.flush()
        df = pd.read_csv(output_csv)
        # print(df)
        # print(df['kendall_corr'].mean())
        # print(df['spearman_corr'].mean())

def stat_dd_count(base_directory, output_csv):
    directories = [d for d in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, d))]
    with open(output_csv, 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Benchmark', 'count of ddmin execution'])
        for directory in directories:
            dir_path = os.path.join(base_directory, directory)
            file = get_data_file(dir_path)
            average_weight_list = get_average_weight_info(file)
            # write to csv
            count = len(average_weight_list)
            csv_writer.writerow([directory, count])

def stat_dd_input_length(base_directory, output_csv):
    directories = [d for d in os.listdir(base_directory) if os.path.isdir(os.path.join(base_directory, d))]
    with open(output_csv, 'w', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['Benchmark', 'input_length_before', 'input_length_after'])
        for directory in directories:
            dir_path = os.path.join(base_directory, directory)
            file = get_data_file(dir_path)
            input_length_list = get_input_length(file)
            # write to csv
            for before, after in input_length_list:
                csv_writer.writerow([directory, before, after])

    # print the mean of the input length before
    df = pd.read_csv(output_csv)
    print(df['input_length_before'].mean())
    print(df['input_length_after'].mean())
    


# =============================================== For Test ================================================== #
        

def test(dir, output):
    sum_num_of_values_before = [0, 0, 0, 0]
    sum_num_of_values_after = [0, 0, 0, 0]
    prob_list = [[] for _ in range(4)]

    directories = [d for d in os.listdir(dir) if os.path.isdir(os.path.join(dir, d))]

    for directory in directories:
        dir_path = os.path.join(dir, directory)
        file = get_data_file(dir_path)
        weight_list = get_weight_list(file)
        for before_reduction, after_reduction in weight_list:
            if len(set(before_reduction)) >= 4 and len(after_reduction) != len(before_reduction):
                unique_before = list(set(before_reduction))
                unique_after = list(set(after_reduction))
                unique_before.sort()
                for i in range(4):
                    num_of_values_before = len([x for x in unique_before if x in unique_before[i*len(unique_before)//4:(i+1)*len(unique_before)//4]])
                    num_of_values_after = len([x for x in unique_after if x in unique_before[i*len(unique_before)//4:(i+1)*len(unique_before)//4]])
                    prob = num_of_values_after / num_of_values_before
                    prob_list[i].append(prob)
                    sum_num_of_values_before[i] += num_of_values_before
                    sum_num_of_values_after[i] += num_of_values_after

    for i in range(4):
        print(f"Sum of values before reduction in part {i}: {sum_num_of_values_before[i]}")
        print(f"Sum of values after reduction in part {i}: {sum_num_of_values_after[i]}")
        print(f"Token reduction in part {i}: {sum_num_of_values_before[i] - sum_num_of_values_after[i]}")
        average_prob = sum(prob_list[i]) / len(prob_list[i])
        print(f"Average prob of part {i}: {average_prob}")
        print(sum_num_of_values_after[i] / sum_num_of_values_before[i])

def main():
    parser = argparse.ArgumentParser(description="Process some integers.")
    parser.add_argument('-d', '--directory', required=True, help='Directory containing subdirectories to process')
    parser.add_argument('-o', '--output', required=True, help='Output CSV file')
    parser.add_argument('-t', '--type', required=True, help='Type of data to process: time_and_query, token, weight')
    
    args = parser.parse_args()

    if not os.path.exists(args.directory):
        raise ValueError(f"Error: Directory {args.directory} does not exist")
    
    if args.type == 'time_and_query':
        get_global_info_all_benchmarks(args.directory, args.output)
    elif args.type == 'token':
        get_global_token_info_all_benchmarks(args.directory, args.output)
    elif args.type == 'weight':
        stat_weight_info(args.directory, args.output)
    elif args.type == 'correlation':
        stat_correlation(args.directory, args.output)
    elif args.type == 'count_dd':
        stat_dd_count(args.directory, args.output)
    elif args.type == 'dd_input_length':
        stat_dd_input_length(args.directory, args.output)
    else:
        test(args.directory, args.output)


if __name__ == '__main__':
    main()

