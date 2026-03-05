buildscript {
    repositories {
        mavenCentral()
        flatDir {
            dirs("../build/libs")
        }
    }
    dependencies {
        classpath("io.github.http-builder-ng:http-builder-ng-core:1.0.4")
        classpath("com.usaa.plugin.gradle:sonar-quality-gates-plugin:3.+")
    }
}

plugins {
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.4"
    java
    id("org.sonarqube") version "4.4.1.3373"
}

apply(plugin = "com.usaa.sonar-quality-gates")

group = "com.usaa.sonar-quality-gates.test"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.test {
    useJUnitPlatform()
}

configure<com.usaa.plugin.gradle.sonarqube.SonarqubeQualityGateExtension> {
    gate = "test-gate"
    profile = "Sonar way"
}
