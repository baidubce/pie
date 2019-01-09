import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.provider.gradleKotlinDslOf

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.10"
    id("idea")
    id("com.google.protobuf") version "0.8.7"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

val kotlinVersion by extra("1.3.10")
val grpcVersion by extra("1.16.1")

group = "com.baidu.acu.pie"
version = "0.8.6-SNAPSHOT"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.4")
    implementation("org.projectlombok:lombok:1.18.4")
    implementation("io.grpc:grpc-netty:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:3.6.1")
    implementation("org.slf4j:slf4j-api:1.7.25")

    testImplementation("org.slf4j:slf4j-simple:1.7.25")
    testImplementation("junit:junit")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.6.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.16.1"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

bintray {
    user = findProperty("user")
    key = findProperty("key")
    setPublications("mavenJava")
    setConfigurations("archives")
    pkg(closureOf<PackageConfig> {
        repo = "MavenRepo"
        name = "audio-streaming-client-java"
        vcsUrl = "https://github.com/baidubce/pie/tree/master/audio-streaming-client-java"
        setLicenses("GPL-3.0")
    })
}