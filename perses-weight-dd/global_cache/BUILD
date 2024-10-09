package(default_visibility = [
    "//visibility:public",
])

py_binary(
    name = "global_cache_server",
    srcs = ["global_cache_server.py"],
)

py_binary(
    name = "run_benchmark_global_cache",
    srcs = ["run_benchmark_global_cache.py"],
)

py_test(
    name = "global_cache_server_test",
    srcs = ["global_cache_server_test.py"],
    deps = ["global_cache_server"],
)

py_test(
    name = "run_benchmark_global_cache_test",
    srcs = ["run_benchmark_global_cache_test.py"],
    deps = ["run_benchmark_global_cache"],
)
