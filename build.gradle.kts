plugins {
    kotlin("multiplatform") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "net.perfectdreams.ddgamehard"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                // Required for WebGL2 Bindings
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2025.2.4")
                implementation(npm("jszip", "3.10.1"))
                implementation("io.ktor:ktor-client-js:3.0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0-RC")
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}