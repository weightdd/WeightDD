import os
import subprocess
import argparse

# 设置命令行参数解析
parser = argparse.ArgumentParser(description="执行命令并将输出重定向到结果文件")
parser.add_argument("directory_A", type=str, help="目录A的路径")
parser.add_argument("result_directory", type=str, help="结果存储目录的路径")
args = parser.parse_args()

# 确保结果目录存在
os.makedirs(args.result_directory, exist_ok=True)

# 遍历目录A下的所有子目录
for subdir in os.listdir(args.directory_A):
    subdir_path = os.path.join(args.directory_A, subdir)
    if os.path.isdir(subdir_path):
        # small.c 和 r.sh 的完整路径
        file_small_c = os.path.join(subdir_path, 'small.c')
        file_r_sh = os.path.join(subdir_path, 'r.sh')

        # 构造命令
        command = f"command1 {file_small_c} {file_r_sh}"

        # 执行命令
        process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = process.communicate()

        # 结果文件的完整路径
        result_file_path = os.path.join(args.result_directory, f"{subdir}result.txt")

        # 写入输出到结果文件
        with open(result_file_path, 'w') as result_file:
            result_file.write(stdout.decode())
            if stderr:
                result_file.write("\nErrors:\n")
                result_file.write(stderr.decode())

print("命令执行完毕，结果已经保存到指定的文件中。")
