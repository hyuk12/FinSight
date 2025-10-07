plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.deps)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.jackson.module.kotlin)

    implementation(project(":libs:common-core"))
    implementation(project(":libs:common-domain"))
    implementation(project(":libs:common-infra"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
