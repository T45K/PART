import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.61"

    // Apply the application plugin to add support for building a CLI application.
    application
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use JDT AST
    implementation("org.eclipse.jdt:org.eclipse.jdt.core:3.20.0")

    // Use guava
    implementation("com.google.guava:guava:28.2-jre")

    // Use Commandline
    implementation("com.github.kusumotolab:sdl4j:0.4.0")

    // Use Rx
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    // Use logger
    implementation("ch.qos.logback:logback-classic:1.1.3")

    // Use args4j
    implementation("args4j:args4j:2.33")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application.
    mainClassName = "io.github.t45k.part.AppKt"
}
