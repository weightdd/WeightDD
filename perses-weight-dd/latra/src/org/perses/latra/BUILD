package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "latra",
    srcs = [
        "AbstractLatraRewriterBuilder.kt",
        "AbstractLatraTransformation.kt",
        "ParsingRelatedArguments.kt",
        "SparAntlrTreeTriple.kt",
        "SparTreeMatch.kt",
        "TransformationUtility.kt",
    ],
    deps = [
        "//:antlr_runtime",
        "//:antlr_tool",
        "//src/org/perses/antlr:antlr_hierarchy",
        "//src/org/perses/antlr:util",
        "//src/org/perses/grammar:grammar_facade",
        "//src/org/perses/grammar/smtlibv2:language_kind",
        "//src/org/perses/grammar/smtlibv2:parser_facade",
        "//src/org/perses/program",
        "//src/org/perses/spartree",
    ],
)

kt_jvm_library(
    name = "TransformationFactory",
    srcs = [
        "AbstractTransformationFactory.kt",
        "CTransformationFactory.kt",
        "SMTTransformationFactory.kt",
    ],
    deps = [
        "//:antlr_runtime",
        "//:antlr_tool",
        "//latra/src/org/perses/latra/c",
        "//latra/src/org/perses/latra/smt",
        "//src/org/perses/antlr:antlr_hierarchy",
        "//src/org/perses/antlr:util",
        "//src/org/perses/grammar:grammar_facade",
        "//src/org/perses/grammar/c:orig_c_grammar",
        "//src/org/perses/grammar/c:orig_parser_facade",
        "//src/org/perses/grammar/smtlibv2:language_kind",
        "//src/org/perses/grammar/smtlibv2:orig_parser_facade",
        "//src/org/perses/grammar/smtlibv2:parser_facade",
        "//src/org/perses/program",
        "//src/org/perses/spartree",
    ],
)
