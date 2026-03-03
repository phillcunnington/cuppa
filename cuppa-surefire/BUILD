load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "cuppa-surefire",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//cuppa",
        "@maven//:org_apache_maven_surefire_surefire_api",
        "@maven//:org_apache_maven_surefire_common_java5",
    ],
)
