package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "smt",
    srcs = [
        "AbstractSMTLatraTransformation.kt",
        "AnnotationRemovalTransformation.kt",
        "BvConverstionTransformation.kt",
        "CheckSatAssuminTransformation.kt",
        "DoubleNegElimTransformation.kt",
        "DoubleNotElimTransformation.kt",
        "EvalFalseTransformation.kt",
        "ExistsRemovalTransformation.kt",
        "ForallRemovalTransformation.kt",
        "LetSubstitutionTransformation.kt",
        "MergeAndTransformation.kt",
        "MergePlusTransformation.kt",
        "MergeVariableTransformation.kt",
        "SubstituteWithConstTransformation.kt",
    ],
    deps = [
        "//:antlr_runtime",
        "//:antlr_tool",
        "//latra/src/org/perses/latra",
        "//src/org/perses/antlr:antlr_hierarchy",
        "//src/org/perses/antlr:util",
        "//src/org/perses/grammar:grammar_facade",
        "//src/org/perses/grammar/smtlibv2:language_kind",
        "//src/org/perses/grammar/smtlibv2:orig_parser_facade",
        "//src/org/perses/program",
        "//src/org/perses/spartree",
    ],
)
