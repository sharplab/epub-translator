import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.charset.StandardCharsets

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.allopen)
    alias(libs.plugins.quarkus)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

val ePubTranslatorVersion: String by project
val isSnapshot: String by project

group = "net.sharplab.epubtranslator"
version = if (isSnapshot.toBoolean()) "$ePubTranslatorVersion-SNAPSHOT" else "$ePubTranslatorVersion.RELEASE"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(enforcedPlatform(libs.quarkus))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-rest-client-jackson")

    implementation(libs.deepl.api)

    testImplementation("io.quarkus:quarkus-junit5")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        javaParameters.set(true)
    }
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("javax.persistence.Entity")
}

configure<io.quarkus.gradle.extension.QuarkusPluginExtension> {
    finalName.set("epub-translator")
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.register<JavaExec>("generateReleaseNote") {
    group = "documentation"
    classpath = files("gradle/lib/github-release-notes-generator.jar")
    args(
        project.version.toString(),
        file("build/release-note.md").absolutePath,
        "--spring.config.location=file:" + file("github-release-notes-generator.yml").absolutePath
    )
}

tasks.register("bumpPatchVersion") {
    group = "documentation"
    doLast {
        val versionProperty = "ePubTranslatorVersion"
        val regex = Regex("^$versionProperty=.*$", RegexOption.MULTILINE)
        val currentVersion = project.property(versionProperty) as String
        val bumpedVersion = bumpPatchVersion(currentVersion)
        val replacement = "$versionProperty=$bumpedVersion"

        val file = file("gradle.properties")
        val original = file.readText(StandardCharsets.UTF_8)
        if (!regex.containsMatchIn(original)) {
            throw GradleException("$versionProperty property not found in gradle.properties")
        }
        val updated = original.replaceFirst(regex, replacement)
        file.writeText(updated, StandardCharsets.UTF_8)
    }
}

tasks.register("switchToSnapshot") {
    group = "documentation"
    doLast {
        val regex = Regex("^isSnapshot=.*$", RegexOption.MULTILINE)
        val replacement = "isSnapshot=true"

        val file = file("gradle.properties")
        val original = file.readText(StandardCharsets.UTF_8)
        if (!regex.containsMatchIn(original)) {
            throw GradleException("isSnapshot property not found in gradle.properties")
        }
        val updated = original.replaceFirst(regex, replacement)
        file.writeText(updated, StandardCharsets.UTF_8)
    }
}

tasks.register("switchToRelease") {
    group = "documentation"
    doLast {
        val regex = Regex("^isSnapshot=.*$", RegexOption.MULTILINE)
        val replacement = "isSnapshot=false"

        val file = file("gradle.properties")
        val original = file.readText(StandardCharsets.UTF_8)
        if (!regex.containsMatchIn(original)) {
            throw GradleException("isSnapshot property not found in gradle.properties")
        }
        val updated = original.replaceFirst(regex, replacement)
        file.writeText(updated, StandardCharsets.UTF_8)
    }
}

tasks.register("updateVersionsInDocuments") {
    group = "documentation"
    doLast {
        // Placeholder for document version updates
    }
}

fun bumpPatchVersion(version: String): String {
    val parts = version.split(".")
    require(parts.size == 3) { "Version must be in the format 'X.Y.Z': \$version" }
    val major = parts[0].toInt()
    val minor = parts[1].toInt()
    val patch = parts[2].toInt() + 1
    return "$major.$minor.$patch"
}
