package com.usaa.plugin.gradle.sonarqube.util

import org.gradle.api.Project

class Helpers {
    static ISonarQubeTask getSonarQubeTask(Project project) {
        Iterator it = project.getTasksByName('sonarqube', true).iterator()
        while (it.hasNext()) {
            return it.next() as ISonarQubeTask
        }
        return null
    }

    /**
     * Retrieves the properties map from the sonarqube task.
     * Newer versions of the sonarqube plugin (4.x+) use MapProperty instead of Map,
     * so we need to call .get() on the MapProperty to retrieve the underlying Map.
     */
    private static Map<String, Object> getPropertiesMap(ISonarQubeTask sqt) {
        def props = sqt.properties
        if (props instanceof Map) {
            return props
        }
        // Handle Gradle MapProperty (sonarqube plugin 4.x+)
        return props.get()
    }

    static String getServerUrl(ISonarQubeTask sqt) {
        return getPropertiesMap(sqt).get('sonar.host.url')
    }

    static String getProjectKey(ISonarQubeTask sqt) {
        return getPropertiesMap(sqt).get('sonar.projectKey')
    }

    static String getBranch(ISonarQubeTask sqt) {
        return getPropertiesMap(sqt).get('sonar.branch')
    }

    static String getProjectName(ISonarQubeTask sqt) {
        return getPropertiesMap(sqt).get('sonar.projectName')
    }

    static boolean sonarPluginExists(Project project) {
        return project.plugins.hasPlugin("org.sonarqube")
    }
}
