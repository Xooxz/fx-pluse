plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    `java-library`
    `maven-publish`
}

group = "com.xooxz"
version = "0.0.1"
description = "fx-common"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.0"))
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    implementation("io.jsonwebtoken:jjwt-api:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ./gradlew publishToMavenLocal
// ~/.m2/repository/com/xooxz/fx-common/0.0.1
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.xooxz"
            artifactId = "fx-common"
            version = "0.0.1"
        }
    }
}
