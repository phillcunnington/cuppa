load("@rules_java//java:defs.bzl", "java_library", "java_test")

java_library(
    name = "cuppa",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [],
)

# Use dynamic test discovery for cuppa core tests
CORE_TESTS = glob(["src/test/java/**/*.java"])
CORE_TEST_CLASSES = [
    src[14:-5].replace("/", ".")
    for src in CORE_TESTS
]

java_test(
    name = "cuppa-tests",
    srcs = CORE_TESTS,
    test_class = "org.testng.TestNG",
    use_testrunner = False,
    main_class = "org.testng.TestNG",
    args = ["-testclass", ",".join(CORE_TEST_CLASSES), "-d", "/tmp/cuppa-tests"],
    deps = [
        ":cuppa",
        "@maven//:org_testng_testng",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_mockito_mockito_all",
        "@maven//:org_beanshell_bsh",
        "@maven//:com_beust_jcommander",
    ],
)
