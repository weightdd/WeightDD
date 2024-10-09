package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

DEFAULT_DEPS = [
    "//src/org/perses/program:language_kind",
    "//src/org/perses/antlr:util",
    "//src/org/perses/antlr:antlr_hierarchy",
    "//src/org/perses/grammar:grammar_facade",
    "//src/org/perses/grammar/c:optc_parser_facade",
    "//src/org/perses/program",
    "//src/org/perses/spartree",
    "//latra/src/org/perses/latra",
    "//latra/src/org/perses/latra/c",
    "//latra/src/org/perses/latra/reduction/reducer:reducer",
    "//test/org/perses:test_util",
    "//:truth",
    "//:guava",
    "//:antlr_tool",
    "//:antlr_runtime",
]

kt_jvm_test(
    name = "LatraReducerTest",
    srcs = [
        "LatraReducerTest.kt",
    ],
    deps = DEFAULT_DEPS,
)
