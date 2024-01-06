plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.21"
    id("de.jensklingenberg.ktorfit") version "1.11.1"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}
group = "ru.snowmaze.barstats"
version = "0.0.5"

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(projects.barStats.app.shared)
    implementation(projects.barStats.library)
    implementation(libs.ktorfit.lib)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okio)
    implementation(libs.kotlinx.serialization.hocon)
    implementation("org.slf4j:slf4j-simple:2.0.10")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("BarStats.jar")
    manifest {
        attributes(mapOf("Main-Class" to "ru.snowmaze.barstats.MainKt"))
    }
}