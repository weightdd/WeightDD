package(default_visibility = ["//buildopt:__subpackages__"])

load(
    "@io_bazel_rules_kotlin//kotlin:jvm.bzl",
    "kt_jvm_library",
)

kt_jvm_library(
    name = "inputs",
    srcs = [
        "BazelReductionInputs.kt",
    ],
    deps = [
        "//buildopt/src/org/perses/bazel/reducer:langauge_kind",
        "//src/org/perses/reduction/io:reduction_inputs_interface",
    ],
)

kt_jvm_library(
    name = "output",
    srcs = [
        "BazelOutputManagerFactory.kt",
    ],
    deps = [
        "//buildopt/src/org/perses/bazel/reducer:build_file",
        "//src/org/perses/reduction/io:output_manager_interface",
    ],
)

kt_jvm_library(
    name = "io",
    srcs = [
        "BazelReductionIOManager.kt",
    ],
    deps = [
        ":inputs",
        ":output",
        "//src/org/perses/reduction/io",
    ],
)
