#!/bin/bash

# 编译small.c
gcc small.c -o small

# 检查编译是否成功
if [ $? -ne 0 ]; then
    echo "编译失败"
    exit 1
fi

# 运行编译后的程序并捕获输出
output=$(./small)

# 检查输出中是否包含"bugs"
if [[ $output == *"bug!"* ]]; then
    exit 0
else
    exit 1
fi


## count the number of lines in small.c
#lines=$(wc -l < small.c)
#
## check if the number of lines is 69
#if [ $lines -eq 289 ]; then
#    exit 0
#else
#    echo "small.c does not have 69 lines, but $lines lines."
#    exit 1
#fi
