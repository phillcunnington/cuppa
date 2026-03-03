load("@rules_java//java:defs.bzl", "java_library", "java_test")

java_library(
    name = "cuppa-junit",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//cuppa",
        "@maven//:junit_junit",
    ],
)

# Use dynamic test discovery for JUnit integration tests
JUNIT_TESTS = glob(["src/test/java/**/*.java"])
JUNIT_TEST_CLASSES = [
    src[14:-5].replace("/", ".")
    for src in JUNIT_TESTS
]

java_test(
    name = "cuppa-junit-tests",
    srcs = JUNIT_TESTS,
    test_class = "org.testng.TestNG",
    use_testrunner = False,
    main_class = "org.testng.TestNG",
    args = ["-testclass", ",".join(JUNIT_TEST_CLASSES), "-d", "/tmp/cuppa-junit-tests"],
    deps = [
        ":cuppa-junit",
        "//cuppa",
        "@maven//:org_testng_testng",
        "@maven//:org_assertj_assertj_core",
        "@maven//:junit_junit",
        "@maven//:org_beanshell_bsh",
        "@maven//:com_beust_jcommander",
    ],
)
