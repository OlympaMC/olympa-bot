import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.nio.file.Path as FilePath

group = "fr.olympa"
version = "1.0.0"
description = "olympa-bot"
java.sourceCompatibility = JavaVersion.VERSION_16

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenLocal()

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "Paper MC"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

configurations {
    all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("fr.olympa:olympa-api")).with(project(":olympa-api"))
        }
    }
}

fun getCheckedOutGitCommitHash(): String {
    val gitFolder = FilePath.of(projectDir.toString(), "..", "/.git").toString()
    val takeFromHash = 8
    /*
     * '.git/HEAD' contains either
     *      in case of detached head: the currently checked out commit hash
     *      otherwise: a reference to a file containing the current commit hash
     */
    val head = File("$gitFolder/HEAD").readText().split(":") // .git/HEAD
    val isCommit = head.size == 1 // e5a7c79edabbf7dd39888442df081b1c9d8e88fd
    // def isRef = head.length > 1     // ref: refs/heads/master

    if (isCommit) return head[0].trim().take(takeFromHash) // e5a7c79edabb

    val refHead = File("$gitFolder/${head[1].trim()}") // .git/refs/heads/master
    return refHead.readText().trim().take(takeFromHash)
}

fun getGitBranch(): String = run {
    val stdout = ByteArrayOutputStream()
    exec {
        setCommandLine("git", "rev-parse", "--abbrev-ref", "--", "HEAD")
        standardOutput = stdout
    }
    return@run stdout.toString().trim()
}

fun getDate() = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

dependencies {
    compileOnly(libs.olympa.api)
    implementation(libs.twitter4j)
    implementation(libs.teamspeak3)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kord.extensions)
    implementation(libs.emoji)
}

kotlin {
    kotlinDaemonJvmArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }

    withType<Jar> {
        archiveFileName.set("OlympaBot.jar")
        destinationDirectory.set(file("build"))
    }

    withType<ShadowJar> {
        archiveFileName.set("OlympaBot.jar")
        destinationDirectory.set(file("build"))
        classifier = null
    }

    processResources {
        outputs.upToDateWhen { false }
        expand(mapOf("plugin_version" to project.version.toString() + "-" + getGitBranch() + '-' + getCheckedOutGitCommitHash() + "-" + getDate()))
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/java")
        }
        resources {
            srcDirs("src/resources")
        }
    }
}
