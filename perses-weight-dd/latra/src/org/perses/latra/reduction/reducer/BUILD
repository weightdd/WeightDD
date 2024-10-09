package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "reducer",
    srcs = ["LatraReducer.kt"],
    deps = [
        "//:antlr_runtime",
        "//:antlr_tool",
        "//latra/src/org/perses/latra",
        "//latra/src/org/perses/latra:TransformationFactory",
        "//latra/src/org/perses/latra/c",
        "//latra/src/org/perses/latra/smt",
        "//src/org/perses/reduction:abstract_reducer",
        "//src/org/perses/spartree",
    ],
)
