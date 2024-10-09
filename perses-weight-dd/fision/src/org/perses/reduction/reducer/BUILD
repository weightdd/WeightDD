package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "reducer",
    srcs = [
        "FisionReducer.kt",
        "MinimalSparTreeGenerator.kt",
    ],
    exports = [
        "//src/org/perses/reduction/reducer/vulcan/pattern",
    ],
    deps = [
        "//:antlr_runtime",
        "//:antlr_tool",
        "//src/org/perses/antlr/atn",
        "//src/org/perses/antlr/atn/tdtree",
        "//src/org/perses/reduction:abstract_reducer",
        "//src/org/perses/reduction:reducer_context_interface",
        "//src/org/perses/reduction/reducer:node_based_reducer",
        "//src/org/perses/spartree",
        "//src/org/perses/util",
        "@maven//:org_apache_commons_commons_text",
    ],
)
