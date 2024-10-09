load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")

kt_jvm_test(
    name = "CacheServerTest",
    srcs = ["CacheServerTest.kt"],
    data = [
        "sample_cache.csv",
    ],
    deps = [
        "//:guava",
        "//:truth",
        "//global_cache/src/org/perses/globalcache:global_cache_server",
        "//src/org/perses/reduction:TestScriptHistory",
    ],
)
