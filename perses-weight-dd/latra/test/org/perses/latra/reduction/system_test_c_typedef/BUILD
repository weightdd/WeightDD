load("//src/org/perses:reduction.bzl", "reduce")
load("//benchmark:benchmark.bzl", "reduction_golden_test")

reduction_golden_test(
    name = "latra_reduction_golden_test_typedef",
    enable_edit_caching = False,
    enable_query_caching = False,
    golden_reduced_file = "golden_reduced_t.c",
    other_flags = {
        "--code-format": "ORIG_FORMAT",
        "--on-demand-reducer-classes": "org.perses.latra.reduction.reducer.LatraReducer",
    },
    perses_bin = "//latra/src/org/perses/latra/reduction:latra_main",
    source_file = "t.c",
    test_script = "r.sh",
)
