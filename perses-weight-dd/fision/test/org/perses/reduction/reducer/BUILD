load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")

kt_jvm_test(
    name = "MinimalSparTreeGeneratorTest",
    srcs = [
        "MinimalSparTreeGeneratorTest.kt",
    ],
    associates = ["//fision/src/org/perses/reduction/reducer"],
    deps = [
        "//:guava",
        "//:truth",
        "//src/org/perses/grammar/c:optc_parser_facade",
        "//test/org/perses:test_util",
    ],
)
