load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

DEFAULT_DEPS = [
    "//src/org/perses/program:language_kind",
    "//src/org/perses/antlr:util",
    "//src/org/perses/antlr:antlr_hierarchy",
    "//src/org/perses/grammar/c:orig_c_grammar",
    "//src/org/perses/grammar/c:orig_parser_facade",
    "//src/org/perses/grammar/smtlibv2:orig_parser_facade",
    "//src/org/perses/program",
    "//src/org/perses/spartree",
    "//latra/src/org/perses/latra",
    "//test/org/perses:test_util",
    "//:truth",
    "//:guava",
    "//:antlr_tool",
    "//:antlr_runtime",
]

kt_jvm_test(
    name = "TransformationUtilityTest",
    srcs = [
        "TransformationUtilityTest.kt",
    ],
    deps = DEFAULT_DEPS,
)

kt_jvm_test(
    name = "CLatraTransformationTest",
    srcs = [
        "CLatraTransformationTest.kt",
    ],
    associates = ["//latra/src/org/perses/latra/c"],
    deps = DEFAULT_DEPS,
)

kt_jvm_test(
    name = "SMTLatraTransformationTest",
    srcs = [
        "SMTLatraTransformationTest.kt",
    ],
    associates = ["//latra/src/org/perses/latra/smt"],
    deps = DEFAULT_DEPS,
)

kt_jvm_test(
    name = "RustLatraTransformationTest",
    srcs = [
        "RustLatraTransformationTest.kt",
    ],
    associates = ["//latra/src/org/perses/latra/rust"],
    deps = DEFAULT_DEPS,
)

kt_jvm_test(
    name = "TransformationFactoryTest",
    srcs = [
        "TransformationFactoryTest.kt",
    ],
    associates = ["//latra/src/org/perses/latra:TransformationFactory"],
    deps = DEFAULT_DEPS,
)
