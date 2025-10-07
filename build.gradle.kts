// root: build.gradle.kts (수정본)
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.deps) apply false
}

allprojects {
    group = "com.finsight"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        the<KotlinJvmProjectExtension>().jvmToolchain(21)
    }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
