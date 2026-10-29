rootProject.name = "jiosaavn-patches"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.0.0"
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

include(":patches")