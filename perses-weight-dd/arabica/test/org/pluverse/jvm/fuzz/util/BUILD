load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "ProbabilityTableTest",
    srcs = ["ProbabilityTableTest.kt"],
    test_class = "org.pluverse.jvm.fuzz.util.ProbabilityTableTest",
    deps = [
        "//:guava",
        "//:truth",
        "//arabica/src/org/pluverse/jvm/fuzz/util",
    ],
)
