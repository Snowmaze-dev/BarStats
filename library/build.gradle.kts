plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    id("de.jensklingenberg.ktorfit") version "1.11.1"
}

kotlin {
    jvm { jvmToolchain(8) }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktorfit.lib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

dependencies {
    with("de.jensklingenberg.ktorfit:ktorfit-ksp:1.11.1") {
        add("kspCommonMainMetadata", this)
        add("kspJvm", this)
        add("kspJvmTest", this)
    }
}