## Sonar Quality Gate Plugin ##

[![CI](https://github.com/usaa/sonar-quality-gates/actions/workflows/ci.yml/badge.svg)](https://github.com/usaa/sonar-quality-gates/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

This Gradle plugin will use the SonarQube API to verify the state of a given project's quality gate(s).

This plugin requires that you also apply and configure the SonarQube plugin and have SonarQube server version 6.3+.

If the project does not already exist within SonarQube, it will be created.

### Version compatibility
| Sonar version | Plugin Version |
|---|---|
| 6.3+ | 3.+ |
| 5.6 - 7.0 | 1.+ |

#### Usage (Kotlin DSL):
```kotlin
// build.gradle.kts
plugins {
    id("org.sonarqube") version "4.4.1.3373"
    id("com.usaa.sonar-quality-gates") version "3.0.0"
}
```

#### Usage (Groovy DSL):
```groovy
// build.gradle
plugins {
    id 'org.sonarqube' version '4.4.1.3373'
    id 'com.usaa.sonar-quality-gates' version '3.0.0'
}
```

#### Gradle Tasks
```
SonarQube Utility tasks
-----------------------
applyQualityGate - Applies SonarQube quality gate to project
applyQualityProfile - Applies SonarQube quality profile to project
verifySonarqubeQualityGates - Verifies no quality gate errors exist for project
```

Ex) `./gradlew applyQualityGate applyQualityProfile sonarqube verifySonarqubeQualityGates` will execute the entire 
workflow, including the sonar scan.

#### Optional Configuration:
Any of the following options are available. You can override one or all as required.

**Kotlin DSL:**
```kotlin
sonarqubeQualityGate {
    sleep = "1000" // the amount of time to sleep between api calls while scan is queued. Time is in milliseconds. Default: 1000
    maxWait = "1800000" // the amount of time to wait for scan to complete before failing. Time is in milliseconds. Default: 1800000 (30min)
    gate = "Sonar way" // override default gate
    profile = "Sonar way" // override default profile
    username = ""
    password = ""
    apiKey = ""
}
```

**Groovy DSL:**
```groovy
sonarqubeQualityGate {
    sleep '1000'
    maxWait '1800000'
    gate 'Sonar way'
    profile 'Sonar way'
    username ''
    password ''
    apiKey ''
}
```

**Default Settings**

```
sleep '1000'
maxWait '1800000'
gate 'SonarQube way'
profile 'Sonar way'
```

#### Permissions
Permissions can be set via the `sonarqubeQualityGate` block or via system properties. Note that precedence is important for determining which value is used.

**Username**
1. Closure
2. sonar.quality.username (System Property)

**Password**
1. Closure
2. sonar.quality.password (System Property)

**Api Key**
1. Closure
2. sonar.quality.login (System Property)
3. sonar.login (System Property)

System properties can be set within the `gradle.properties` file.
```properties
systemProp.sonar.login=<token>
```


Note: This project is not affiliated in anyway with the SonarQube project.
