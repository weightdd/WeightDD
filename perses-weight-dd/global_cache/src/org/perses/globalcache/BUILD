package(
    default_visibility = ["//visibility:public"],
)

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "global_cache_server",
    srcs = [
        "CacheServer.kt",
        "CacheServerHandler.kt",
    ],
    deps = [
        "//:flogger",
        "//:guava",
        "//:jackson",
        "//:netty",
        "//src/org/perses/reduction:TestScriptHistory",
    ],
)
