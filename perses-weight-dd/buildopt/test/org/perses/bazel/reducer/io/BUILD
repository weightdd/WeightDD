load(
    "@io_bazel_rules_kotlin//kotlin:jvm.bzl",
    "kt_jvm_test",
)

kt_jvm_test(
    name = "BazelReductionInputsTest",
    srcs = [
        "BazelReductionInputsTest.kt",
    ],
    deps = [
        "//:kotlin_stdib",
        "//:truth",
        "//buildopt/src/org/perses/bazel/reducer/io:inputs",
    ],
)

kt_jvm_test(
    name = "BazelOutputManagerFactoryTest",
    srcs = [
        "BazelOutputManagerFactoryTest.kt",
    ],
    deps = [
        "//:kotlin_stdib",
        "//:truth",
        "//buildopt/src/org/perses/bazel/reducer/io:inputs",
        "//buildopt/src/org/perses/bazel/reducer/io:output",
        "//buildopt/test/org/perses/bazel/reducer:util",
    ],
)
