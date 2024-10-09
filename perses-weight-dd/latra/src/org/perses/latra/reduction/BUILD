package(default_visibility = [
    "//visibility:public",
])

java_binary(
    name = "latra_main",
    jvm_flags = ["-ea"],
    main_class = "org.perses.Main",
    runtime_deps = [
        "//atn/src/org/perses/reduction/reducer",
        "//latra/src/org/perses/latra/reduction/reducer",
        "//src/org/perses:perses_lib",
    ],
)
