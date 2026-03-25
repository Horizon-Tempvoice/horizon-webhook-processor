import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.lang.Closure

val targetJdkVersion = "25"
val ktorVersion = "3.4.1"
val exposedVersion = "1.1.1"

plugins {
    kotlin("jvm") version "2.3.20"
    application
    kotlin("plugin.serialization") version "2.3.20"
    id("com.gradleup.shadow") version "9.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("com.palantir.git-version") version "5.0.0"
    id("com.google.cloud.tools.jib") version "3.5.3"
}

group = "cloud.horizonbot"
val gitVersion: Closure<String> by extra
version = gitVersion()

repositories {
    mavenCentral()
}

application {
    mainClass.set("cloud.horizonbot.webhookprocessor.MainKt")
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.postgresql:postgresql:42.7.10")

    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(targetJdkVersion.toInt())
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("horizon-webhook-processor.jar")
    mergeServiceFiles()
}

jib {
    from {
        image = "eclipse-temurin:25-jre-alpine"
    }
    container {
        mainClass = "cloud.horizonbot.webhookprocessor.MainKt"
        jvmFlags = listOf("-XX:+UseContainerSupport")
        ports = listOf("8080")
    }
}