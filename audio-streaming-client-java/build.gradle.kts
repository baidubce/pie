import com.google.protobuf.gradle.*
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.10"
    id("idea")
    id("com.google.protobuf") version "0.8.8"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

val kotlinVersion by extra("1.3.10")
val grpcVersion by extra("1.16.1")

group = "com.baidu.acu.pie"
version = "1.1.0.SNAPSHOT"

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.7"
    targetCompatibility = "1.7"
}

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
    compile("io.grpc:grpc-stub:$grpcVersion")
    compile("com.google.protobuf:protobuf-java:3.6.1")
    implementation("org.slf4j:slf4j-api:1.7.25")
    compile("joda-time:joda-time:2.10.1")

    testImplementation("org.slf4j:slf4j-simple:1.7.25")
    testImplementation("junit:junit")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.1.RELEASE")
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

tasks.register<Jar>("sourceJar") {
    from(sourceSets["main"].allSource)
    classifier = "sources"
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
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