package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "c",
    srcs = [
        "AbstractCLatraTransformation.kt",
        "FunctionInliningLatraTransformation.kt",
        "TypeDefTransform.kt",
        "VoidReturnFunctionDeclLatraTransformation.kt",
        "VoidReturnFunctionDefLatraTransformation.kt",
    ],
    deps = [
        "//:antlr_runtime",
        "//:antlr_tool",
        "//latra/src/org/perses/latra",
        "//src/org/perses/antlr:antlr_hierarchy",
        "//src/org/perses/antlr:util",
        "//src/org/perses/grammar:grammar_facade",
        "//src/org/perses/grammar/c:orig_parser_facade",
        "//src/org/perses/grammar/smtlibv2:language_kind",
        "//src/org/perses/grammar/smtlibv2:parser_facade",
        "//src/org/perses/program",
        "//src/org/perses/spartree",
    ],
)
