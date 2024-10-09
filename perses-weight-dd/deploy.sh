#!/bin/bash

# 本地文件路径
#local_file="./bazel-bin/src/org/perses/perses_deploy.jar"

local_file=$1

host_name=$2
# 远程服务器信息
remote_user="x27zhou"
remote_host="${host_name}.cs.uwaterloo.ca"
#remote_host="cartesian.cs.uwaterloo.ca"
#remote_host="lambda.cs.uwaterloo.ca"
remote_path="/home/x27zhou/perses_deploy.jar"

# Docker 容器信息
# let conrainer_name equals first argument
container_name=$3
#container_path="/tmp/binaries/perses_deploy.jar"
container_path=$4

# 检查hostname和container_name是否存在
if [ -z "$container_name" ]; then
    # print usage
    echo "Usage: deploy.sh <file_path> <server_name> <container_name> <container_path>"
    exit 1
fi

# 检查本地文件是否存在
if [ ! -f "$local_file" ]; then
    echo "本地文件不存在: $local_file"
    exit 1
fi

echo "Start deploying $local_file to $remote_host:$remote_path"

# 删除远程服务器上的同名文件（如果存在）
ssh $remote_user@$remote_host "rm -f $remote_path"

echo "Copying $local_file to $remote_host:$remote_path"

# 复制文件到远程服务器
scp $local_file $remote_user@$remote_host:$remote_path
if [ $? -ne 0 ]; then
    echo "文件复制到服务器失败"
    exit 1
fi

echo "Copying $local_file to $remote_host:$remote_path succeeded"

# 在远程服务器上执行 Docker 相关操作
ssh $remote_user@$remote_host bash -c "'
    # 检查 Docker 容器是否运行
    if ! docker ps -q --filter name=^/$container_name\$; then
        # 启动 Docker 容器
        docker start $container_name
        if [ $? -ne 0 ]; then
            echo \"启动 Docker 容器失败\"
            exit 1
        fi
    fi

    # 复制文件到 Docker 容器，并删除容器中的同名文件（如果存在）
    docker exec $container_name rm -f $container_path
    docker cp $remote_path $container_name:$container_path
    if [ $? -ne 0 ]; then
        echo \"文件复制到 Docker 容器失败\"
        exit 1
    fi
'"

# 检查最后一步操作是否成功
if [ $? -ne 0 ]; then
    echo "远程服务器上的操作失败"
    exit 1
fi

echo "操作成功完成"
