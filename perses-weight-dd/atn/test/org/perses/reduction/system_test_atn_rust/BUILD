load("//src/org/perses:reduction.bzl", "reduce")
load("//benchmark:benchmark.bzl", "reduction_golden_test")

reduction_golden_test(
    name = "atn_reduction_rust_golden_test",
    golden_reduced_file = "golden_reduced_t.rs",
    other_flags = {
        "--on-demand-reducer-classes": "org.perses.reduction.reducer.AtnFineTuningTokenReducer",
    },
    perses_bin = "//atn/src/org/perses/reduction:atn_perses_main",
    source_file = "t.rs",
    test_script = "r.sh",
)
