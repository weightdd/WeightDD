package(default_visibility = [
    "//visibility:public",
])

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

java_binary(
    name = "java_generator_bin",
    srcs = ["FuzzerMain.java"],
    jvm_flags = ["-ea"],
    main_class = "org.pluverse.jvm.fuzz.FuzzerMain",
    deps = [
        ":java_fuzzer_runner",
        "//:guava",
        "//src/org/perses/util/cmd",
    ],
)

kt_jvm_library(
    name = "java_fuzzer_runner",
    srcs = [
        "CommandOptions.kt",
        "JavaFuzzer.kt",
        "TestInstance.kt",
    ],
    deps = [
        "//:guava",
        "//arabica/src/org/pluverse/jvm/fuzz/basic",
        "//arabica/src/org/pluverse/jvm/fuzz/util",
        "//src/org/perses/util/cmd",
        "//src/org/perses/util/java",
    ],
)
