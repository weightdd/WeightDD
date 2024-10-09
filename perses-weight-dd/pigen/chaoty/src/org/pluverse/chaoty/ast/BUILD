package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "ast",
    srcs = glob(["*.kt"]),
    deps = [
        "//pigen/ast/src/org/pigen/python/ast",
        "//src/org/perses/util",
    ],
)
