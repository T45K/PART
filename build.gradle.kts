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
    compile(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use JDT AST
    compile("org.eclipse.jdt:org.eclipse.jdt.core:3.20.0")

    // Use guava
    compile("com.google.guava:guava:28.2-jre")

    // Use Commandline
    compile("com.github.kusumotolab:sdl4j:0.4.0")

    // Use Rx
    compile("io.reactivex.rxjava2:rxkotlin:2.4.0")

    // Use logger
    compile("ch.qos.logback:logback-classic:1.1.3")

    // Use args4j
    compile("args4j:args4j:2.33")

    // Use sqlite
    compile("org.xerial:sqlite-jdbc:3.30.1")

    // Use the Kotlin test library.
    testCompile("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application.
    mainClassName = "io.github.t45k.part.PartMainKt"
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "io.github.t45k.part.PartMainKt"
    }

    from(
            configurations.compile.get().map {
                if (it.isDirectory) it else zipTree(it)
            }
    )
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}
