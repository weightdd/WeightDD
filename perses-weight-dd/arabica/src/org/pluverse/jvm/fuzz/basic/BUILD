package(default_visibility = ["//visibility:public"])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "basic",
    srcs = glob(["*.kt"]),
    deps = [
        "//arabica/src/org/pluverse/jvm/fuzz/util",
    ],
)
