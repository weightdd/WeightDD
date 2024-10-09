#!/usr/bin/env bash

set -o xtrace
# This script cannot be run directly, it need to be modified and invoked by run_benchmark.py

# Check whether database server is available
ping=$(echo -e "p\n" | nc localhost "${CACHE_QUERY_SOCKET_PORT}")
if [[ "$ping" == "PONG" ]]; then
    # Calculate the SHA512 of the program
    SHA512_DIGEST=$(sha512sum "${SOURCE_NAME}" | cut -d " " -f 1)
    readonly SHA512_DIGEST
    result=$(echo -e "q\n${SHA512_DIGEST}\n" | nc localhost "${CACHE_QUERY_SOCKET_PORT}")
    if [[ "$result" == "cache miss" ]]; then
        start=$(date +%s.%N)
        cp "${WRAPPED_SCRIPT_PATH}" .
        ./"${WRAPPED_SCRIPT_NAME}"
        ret=$?
        end=$(date +%s.%N)
        runtime=$( echo "$end - $start" | bc -l )
        echo -e "u\n${SHA512_DIGEST}\n${ret}\n${runtime}\n" | nc localhost "${CACHE_QUERY_SOCKET_PORT}"
        exit "$ret"
    fi
    exit "${result}"
else
    cp "${WRAPPED_SCRIPT_PATH}" .
    ./"${WRAPPED_SCRIPT_NAME}"
    ret=$?
    exit "$ret"
fi
