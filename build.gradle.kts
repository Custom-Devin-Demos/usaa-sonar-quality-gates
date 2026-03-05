plugins {
    groovy
    jacoco
    id("com.gradle.plugin-publish") version "1.2.1"
    `java-gradle-plugin`
}

version = if (System.getenv("GITHUB_REF_TYPE") == "tag") version.toString() else "${version}-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("io.github.http-builder-ng:http-builder-ng-core:1.0.4")
    implementation("org.apache.maven:maven-artifact:3.3.3")

    testImplementation(localGroovy())
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0") {
        exclude(group = "org.codehaus.groovy")
    }
    testImplementation(gradleTestKit())
}

sourceSets {
    create("integrationTest") {
        groovy.srcDir("src/integTest/groovy")
        resources.srcDir("src/integTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
    create("functionalTest") {
        groovy.srcDir("src/functTest/groovy")
        resources.srcDir("src/functTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

configurations {
    val integrationTestImplementation by getting {
        extendsFrom(configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly by getting {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
    val functionalTestImplementation by getting {
        extendsFrom(configurations.testImplementation.get())
    }
    val functionalTestRuntimeOnly by getting {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.named("groovydoc"))
    archiveClassifier.set("javadoc")
    from(tasks.named<Groovydoc>("groovydoc").get().destinationDir)
}

tasks.named<Groovydoc>("groovydoc") {
    link("https://docs.oracle.com/en/java/javase/11/docs/api/", "java.", "javax.")
    link("https://docs.groovy-lang.org/${groovy.lang.GroovySystem.getVersion()}/html/api/", "groovy.", "org.codehaus.groovy.")
    link("https://docs.gradle.org/${project.gradle.gradleVersion}/javadoc/", "org.gradle.")
}

artifacts {
    archives(tasks.named("sourcesJar"))
    archives(tasks.named("javadocJar"))
}

gradlePlugin {
    website.set("https://usaa.github.io/sonar-quality-gates")
    vcsUrl.set("https://www.github.com/usaa/sonar-quality-gates")
    plugins {
        create("sonarPlugin") {
            id = "com.usaa.sonar-quality-gates"
            implementationClass = "com.usaa.plugin.gradle.sonarqube.SonarqubeQualityGatePlugin"
            displayName = "Sonar Quality Gate Plugin"
            description = "Sonar quality gate/profile administration and gate verification."
            tags.set(listOf("sonar", "admin", "gate", "quality-gate"))
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mustRunAfter(tasks.test)
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}
