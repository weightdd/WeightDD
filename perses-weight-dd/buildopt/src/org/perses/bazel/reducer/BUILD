package(default_visibility = ["//buildopt:__subpackages__"])

load(
    "@io_bazel_rules_kotlin//kotlin:jvm.bzl",
    "kt_jvm_library",
)

kt_jvm_library(
    name = "command_options",
    srcs = ["CommandOptions.kt"],
    deps = [
        "//src/org/perses/util/cmd",
    ],
)

java_library(
    name = "buildopt_lib",
    exports = [
        ":command_options",
    ],
)

java_binary(
    name = "buildopt",
    srcs = ["Main.java"],
    deps = [
        ":buildopt_lib",
        "//src/org/perses/util/cmd",
    ],
)

kt_jvm_library(
    name = "reduction_driver",
    srcs = [
        "DepsReductionDriver.kt",
        "Setup.kt",
    ],
    deps = [
        ":ReductionScriptTemplate",
        ":command_options",
        ":langauge_kind",
        "//buildopt/src/org/perses/bazel/reducer/io",
        "//src/org/perses/reduction:reduction_driver_interface",
    ],
)

kt_jvm_library(
    name = "langauge_kind",
    srcs = [
        "LanguageStarlark.kt",
    ],
    deps = [
        "//src/org/perses/program:language_kind",
    ],
)

kt_jvm_library(
    name = "build_file",
    srcs = [
        "BuildFileSet.kt",
    ],
    deps = [
        ":langauge_kind",
        "//buildopt/src/org/perses/bazel/reducer/io:inputs",
        "//src/org/perses/program:language_kind",
        "//src/org/perses/program:source_file",
        "//src/org/perses/util",
    ],
)

kt_jvm_library(
    name = "ReductionScriptTemplate",
    srcs = [
        "ReductionScriptTemplate.kt",
    ],
    deps = [
        "//:guava",
        "//src/org/perses/util",
        "//src/org/perses/util:AbstractBashScriptGenerator",
    ],
)
