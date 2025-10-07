plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}


dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))
    api("org.springframework:spring-web")  // public API 노출이면 api로
    implementation(libs.jackson.module.kotlin)
    implementation(project(":libs:common-domain"))
}