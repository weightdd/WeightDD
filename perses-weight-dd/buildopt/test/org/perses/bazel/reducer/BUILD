package(
    default_visibility = ["//buildopt:__subpackages__"],
)

load(
    "@io_bazel_rules_kotlin//kotlin:jvm.bzl",
    "kt_jvm_library",
    "kt_jvm_test",
)

kt_jvm_test(
    name = "ReductionScriptTemplateTest",
    srcs = [
        "ReductionScriptTemplateTest.kt",
    ],
    deps = [
        ":util",
        "//:kotlin_stdib",
        "//:truth",
        "//buildopt/src/org/perses/bazel/reducer:ReductionScriptTemplate",
        "//buildopt/src/org/perses/bazel/reducer:build_file",
    ],
)

kt_jvm_test(
    name = "BuildFileSetTest",
    srcs = [
        "BuildFileSetTest.kt",
    ],
    deps = [
        ":util",
        "//:kotlin_stdib",
        "//:truth",
        "//buildopt/src/org/perses/bazel/reducer:build_file",
    ],
)

kt_jvm_test(
    name = "SetupTest",
    srcs = [
        "SetupTest.kt",
    ],
    deps = [
        "//:truth",
        "//buildopt/src/org/perses/bazel/reducer:reduction_driver",
    ],
)

kt_jvm_library(
    name = "util",
    srcs = [
        "BuildOptTestUtil.kt",
    ],
    deps = [
        "//buildopt/src/org/perses/bazel/reducer/io:inputs",
        "//src/org/perses/util",
    ],
)
