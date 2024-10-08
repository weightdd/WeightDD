#!/usr/bin/env python3

import argparse
import json
import re
import os
import subprocess
import tempfile
from datetime import datetime
from dataclasses import dataclass
from concurrent.futures import ProcessPoolExecutor
from typing import List, Dict, Tuple

__location__ = os.path.realpath(
    os.path.join(os.getcwd(), os.path.dirname(__file__)))

def parse_arguments():
    parser = argparse.ArgumentParser(description="Runs the benchmark and output results in JSON")
    parser.add_argument("-s", "--subjects", nargs='+', default=[],
                        help="Benchmark with specified subject(s), separated by spaces if more than one.")
    parser.add_argument("-i", "--iterations", type=int, default=1,
                        help="Repeat the benchmark for the number of times specified")
    parser.add_argument("-ss", "--show-subprocess", action="store_true", default=False,
                        help="Show all pipe stdout and stderr")
    parser.add_argument("-r", "--reducers", nargs='+', default=[],
                        help="Specify scripts for running the reducers for benchmarking. "
                            "Separate them with space and make sure the scripts are in the binaries folder. ")
    parser.add_argument("-mp", "--memory-profiler", action="store_true", default=False,
                        help="Enable Perses memory profiler with -Xlog")
    parser.add_argument("-o", "--output-dir", default=None, type=str,
                        help="Export benchmark results to a specified directory. "
                            "The default is to save results to the benchmark_results folder.")
    parser.add_argument("-j", "--jobs", type=int, default=1,
                        help="Number of parallel jobs to run. Default is 1.")
    return parser.parse_args()


@dataclass
class Parameter:
    # Default field values
    benchmark_target: List[str]
    reducers: List[str]
    show_subprocess: bool
    iterations: int
    memory_profiler: bool
    output_dir: str
    jobs: int

    def __post_init__(self):
        self.validate()

    def validate(self):
        # validate parameters
        # benchmark_target
        if not self.benchmark_target:
            raise Exception('Error: No Subject(s)')
        for subject_name in self.benchmark_target:
            folder_path = os.path.join(__location__, subject_name)
            if not os.path.exists(folder_path):
                raise Exception(f'Error: Folder path not found: {folder_path}')
        # iterations
        if self.iterations < 1:
            raise Exception('Error: Invalid ITERATIONS value')
        # reducers
        if not self.reducers:
            raise Exception('Error: No reducers detected')
        if len(self.reducers) != len(set(self.reducers)):
            raise Exception('Error: Duplicated reducers detected')
        # output directory
        if self.output_dir:
            self.output_dir = os.path.abspath(self.output_dir)
            if not os.path.exists(self.output_dir):
                os.makedirs(self.output_dir)
        else:
            general_result_dir = "benchmark_results"
            general_result_subdir = f"benchmark_{datetime.now().strftime('%Y%m%d-%H%M%S')}"
            self.output_dir = os.path.join(__location__, general_result_dir, general_result_subdir)
            os.makedirs(self.output_dir)


def extract_info_properties(subject_name: str) -> Tuple[str, str]:
    """Extract script file and source file from a subject folder"""
    info_dict = dict()

    # validate info.properties path
    info_properties_path = os.path.join(__location__, subject_name, "info.properties")
    if not os.path.exists(info_properties_path):
        raise Exception(f'Error: info.properties not found: {info_properties_path}')

    with open(info_properties_path, 'r') as target_file:
        temp_list = target_file.read().splitlines()
    for entry in temp_list:
        buf = entry.split('=')
        info_dict[buf[0]] = buf[1]

    # validate source file & script file path
    if "source_file" not in info_dict:
        raise Exception('Error: No source_file found in info.properties')
    if "script_file" not in info_dict:
        raise Exception('Error: No script_file found in info.properties')

    source_file_path = os.path.join(__location__, subject_name, info_dict["source_file"])
    script_file_path = os.path.join(__location__, subject_name, info_dict["script_file"])

    if not os.path.exists(source_file_path):
        raise Exception('Error: source_file not found: {}'.format(source_file_path))
    if not os.path.exists(script_file_path):
        raise Exception('Error: script_file not found: {}'.format(script_file_path))

    return source_file_path, script_file_path


def count_token(source_file_path: str) -> int:
    try:
        token_counter_sh = os.path.join(__location__, "binaries", "run_token_counter.sh")
        count = subprocess.check_output(
            [token_counter_sh, source_file_path],
            stderr=subprocess.STDOUT)
        return int(count)
    except Exception as err:
        print("Error counting token for " + source_file_path)
        raise err


def check_java_version():
    """Validate java version must be 9+, so Xlog is supported"""
    java_version = subprocess.check_output(['java', '-version'], stderr=subprocess.STDOUT)
    pattern = '\"(\d.+)\"'
    version_number = re.search(pattern, java_version.decode("utf-8"))[0]
    major_version = version_number[1:].split('.')[0]

    if int(major_version) < 9:
        raise Exception(f"java version: {major_version} detected. -Xlog not supported below java 9")


def environment_updater(memory_flag: bool, filename_log: str) -> Dict[str, str]:
    """Update JVM memory settings if memory_profiler enabled"""
    environment = os.environ.copy()
    if memory_flag:
        # validate java version
        check_java_version()
        # add Xlog environment variable for memory profiling
        xlog_flag = f'-Xlog:gc+heap=debug:file={os.path.join(__location__, filename_log)}'
        jvm_flags = os.environ.get('JVM_FLAGS')
        if jvm_flags:
            environment['JVM_FLAGS'] = f"{jvm_flags} {xlog_flag}"
        else:
            environment['JVM_FLAGS'] = f"{xlog_flag}"
        return environment
    else:
        return environment


def filename_generator(subject: str, reducer: str, iteration: int, timemark: str) -> Tuple[str, str]:
    """Generate proper names for report (and memory log if applicable)"""
    subject = os.path.basename(subject)
    filename_report = f"stat_{subject}_{reducer}_{timemark}_itr{iteration}.txt"
    filename_log = f"tmp_{subject}_{reducer}_{timemark}_itr{iteration}.log"

    return filename_report, filename_log


def get_extra_flags(reducer: str) -> str:
    """Extract extra flags (enviromental variables).
        currently only Perses is supported"""
    if reducer == 'perses':
        flag = os.environ.get('PERSES_FLAGS')
        return flag if flag else "Default"


def report_generator(subject, reducer, reducer_flags, token_count, iteration, run_result: list):
    """Report initialization"""
    report = dict()
    report['Subject'] = subject
    report['Reducer'] = reducer
    report["Environment"] = reducer_flags
    report["Origin token count"] = token_count
    report["Iteration"] = iteration
    report["Query"] = int(run_result[3])
    report["Time"] = int(run_result[4])
    report["Token_remaining"] = int(run_result[5])
    report["Ret_code"] = int(run_result[6])
    return report


def output_manager(output_dir: str, filename_report: str, report: dict):
    """Manage sub-directory creation and output file"""
    output_filepath = os.path.join(output_dir, filename_report)
    # store benchmark to json file
    json_object = json.dumps(report, indent=4)
    print(json_object)
    with open(output_filepath, 'w') as out_file:
        out_file.write(json_object)
    print(f'Output path: {output_filepath}')

def run_process(
        subject: str,
        reducer: str,
        iteration: int,
        time: str,
        memory_profiler_flag: bool,
        show_subprocess_flag: bool,
        reducer_flags: str,
        output_dir: str):
    print(f"Using {reducer} to reduce {subject}\n***** Iteration: {iteration} *****")

    source_file_path, script_file_path = extract_info_properties(subject)
    token_count = count_token(source_file_path)

    # create report filename (and log filename if applicable)
    filename_report, filename_log = filename_generator(subject, reducer, iteration, time)

    # setup environment variables for subprocess
    environment = environment_updater(memory_profiler_flag, filename_log)

    # tmp file for reducer's output
    fd, fname = tempfile.mkstemp()
    os.close(fd)

    # run reduce scripts
    pipe = None if show_subprocess_flag else subprocess.PIPE

    subject_name = os.path.basename(subject)
    reducer_script = os.path.join(__location__, "binaries", f"run_{reducer}.sh")
    output_dir = os.path.join(output_dir, f"{reducer}_{iteration}", subject_name)
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    proc = subprocess.run(
        [reducer_script,
        script_file_path,
        source_file_path,
        output_dir,
        fname],
        check=False,
        stdout=pipe,
        stderr=pipe,
        env=environment)

    # retrieve reduction results
    run_result = []
    with open(fname, "r") as output:
        run_result = output.read().strip().split("\n")
        if len(run_result) != 7:
            run_result = []
    os.remove(fname)
    if not run_result:
        err_msg = proc.stderr.decode()
        out_msg = proc.stdout.decode()
        print(f"Error: {reducer_script} failed to run. \nstderr: {err_msg}\nstdout: {out_msg}")
        return

    # report = report_generator(subject, reducer, reducer_flags, token_count, iteration, run_result)
    # output_manager(output_dir, filename_report, report)
    output_filepath = os.path.join(output_dir, filename_report)
    # store benchmark to json file
    # json_object = json.dumps(report, indent=4)
    # print(json_object)
    with open(output_filepath, 'w') as out_file:
        out_file.write(proc.stdout.decode())
    print(f'Output path: {output_filepath}')


def main():
    # parameter handler
    args = parse_arguments()
    parameter = Parameter(
        args.subjects,
        args.reducers,
        args.show_subprocess,
        args.iterations,
        args.memory_profiler,
        args.output_dir,
        args.jobs
    )
    print(parameter)

    executor = ProcessPoolExecutor(max_workers=parameter.jobs)
    futures = []
    # benchmark starts here
    for subject in parameter.benchmark_target:
        if subject[-1] == "/":
            subject= subject[:-1]

        # reduction
        for reducer in parameter.reducers:
            # unique time mark distinguishes different configurations
            time = datetime.now().strftime("%Y%m%d-%H%M%S-%f")
            reducer_flags = get_extra_flags(reducer)

            # only one iteration if reducer is 'perses_wprobdd' or 'hdd_wprobdd'
            if reducer == 'perses_wprobdd' or reducer == 'hdd_wprobdd':
                futures.append(executor.submit(
                    run_process,
                    subject,
                    reducer,
                    0,
                    time,
                    parameter.memory_profiler,
                    parameter.show_subprocess,
                    reducer_flags,
                    parameter.output_dir))
            else:
                # iteration
                for iteration in range(parameter.iterations):
                    futures.append(executor.submit(
                        run_process,
                        subject,
                        reducer,
                        iteration,
                        time,
                        parameter.memory_profiler,
                        parameter.show_subprocess,
                        reducer_flags,
                        parameter.output_dir))

    for future in futures:
        future.result()

if __name__ == "__main__":
    main()
