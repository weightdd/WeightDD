package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "ast",
    srcs = glob(["*.kt"]),
    deps = [
        "//src/org/perses/util",
        "//src/org/perses/util/ast",
    ],
)
