package com.usaa.plugin.gradle.sonarqube.util

import com.usaa.plugin.gradle.sonarqube.exceptions.QualityGateApplyFailedException
import com.usaa.plugin.gradle.sonarqube.exceptions.SonarCreateProjectFailedException

import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.HttpException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static groovyx.net.http.HttpBuilder.configure

class SonarqubeRestClient {

    enum ScanStatus {
        QUEUED,
        IN_PROGRESS,
        COMPLETE
    }

    enum QualityGateStatus {
        OK,
        ERROR,
        NONE
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

    private static final Logger logger = LoggerFactory.getLogger(SonarqubeRestClient.class)
    
    protected static final String AUTH_HEADER_KEY = "Authorization"

    protected final HttpBuilder http
    protected final String authHeader

    SonarqubeRestClient(String serverUrl) {
        this.http = configure {
            request.uri = serverUrl
            request.contentType = 'application/json'
        }
    }

    SonarqubeRestClient(String serverUrl, String username, String password) {
        this(serverUrl)
        this.authHeader = "Basic " + (username + ":" + password).bytes.encodeBase64().toString()
    }

    SonarqubeRestClient(String serverUrl, String apiKey) {
        this(serverUrl, apiKey, "")
    }

    String getComponentId(String projectKey) {
        logger.debug('Getting Component id for projectKey<{}>', projectKey)
        http.get {
            request.uri.path = '/api/components/show'
            request.uri.query = [key: projectKey]
            response.when(404) { FromServer fs, Object body ->
                // Added additional output here because standard output of 'Not Found' isn't very valuable.
                // Other methods don't need because if one is available, the rest will exist.
                logger.error('Unable to find SonarQube component with projectKey <{}>', projectKey)
                throw new HttpException(fs, body)
            }
            response.success { FromServer fs, Object body ->
                return body.component.id
            }
        }
    }

    ScanStatus getScanStatus(String projectKey) {
        logger.debug('Getting scan status for component <{}>', projectKey)
        http.get {
            request.uri.path = '/api/ce/component'
            request.uri.query = [component: projectKey]
            response.success { FromServer fs, Object body ->
                if (body.queue.size() > 0) {
                    return ScanStatus.QUEUED
                } else if (body.current.status == 'SUCCESS') {
                    return ScanStatus.COMPLETE
                } else if (body.current.status == 'IN_PROGRESS') {
                    return ScanStatus.IN_PROGRESS
                } else {
                    return ScanStatus.FAILED
                }
            }
        }
    }

    QualityGateStatus getQualityGateStatus(String projectKey) {
        logger.debug('Getting quality gate status for projectKey <{}>', projectKey)

        http.get {
            request.uri.path = '/api/qualitygates/project_status'
            request.uri.query = [projectKey: projectKey]
            response.success { FromServer fs, Object body ->
                if (body.errors && body.errors.size() > 0) {
                    logger.debug("Error checking quality gate: {}", body.errors.toString())
                }
                return QualityGateStatus.valueOf(body.projectStatus.status)
            }
        }
    }

    boolean projectExists(String projectKey) {
        logger.debug('Search for projectKey <{}>', projectKey)
        http.get {
            request.uri.path = '/api/projects/search'
            request.headers[AUTH_HEADER_KEY] = authHeader
            request.uri.query = [projects: projectKey]
            response.success { FromServer fs, Object body ->
                if (body.components.size() > 0) {
                    return true
                }
                return false
            }
        }
    }

    String getQualityGateId(String qualityGateName) {
        logger.debug('Getting quality gate id for name <{}>', qualityGateName)
        http.get {
            request.uri.path = '/api/qualitygates/list'
            response.success { FromServer fs, Object body ->
                for (gate in body.qualitygates) {
                    if (gate.name == qualityGateName) {
                        logger.debug("Gate {} found with id {}", qualityGateName, gate.id)
                        return gate.id
                    }
                }
                logger.debug("Gate {} not found", qualityGateName)
                return null
            }
        }
    }

    boolean applyQualityGate(String projectKey, String gateId) {
        logger.debug('Applying quality gate <{}> to project <{}>', gateId, projectKey)

        http.post {
            request.uri.path = '/api/qualitygates/select'
            request.headers[AUTH_HEADER_KEY] = authHeader
            request.contentType = 'application/json'
            request.body = [
                    "projectKey": projectKey,
                    "gateId": gateId,
            ]
            response.success { FromServer fs, Object body ->
                logger.debug('Apply gate response code: {}', fs.statusCode)
                if (fs.statusCode != 204) {
                    throw new QualityGateApplyFailedException("Unable to apply gate: " + gateId + " to project " + projectKey +
                         ". error: " + fs.statusCode)
                }
                return true
            }
        }
    }

    boolean applyQualityProfile(String projectKey, String profileName, String language) {
        logger.debug('Applying quality profile <{}> to project <{}> for language <{}>', profileName, projectKey, language)

        http.post {
            request.uri.path = '/api/qualityprofiles/add_project'
            request.headers[AUTH_HEADER_KEY] = authHeader
            request.contentType = 'application/json'
            request.body = [
                    "projectKey": projectKey,
                    "profileName": profileName,
                    "language": language
            ]
            response.success { FromServer fs, Object body ->
                logger.debug('Apply profile response code: {}', fs.statusCode)
                if (fs.statusCode != 204) {
                    throw new QualityGateApplyFailedException(sprintf("Unable to apply profile: %s to project %s. error: %s",
                            profileName, projectKey, fs.statusCode))
                }
                return true
            }
        }
    }

    @SuppressWarnings('FactoryMethodName')
    String createProject(String projectKey, String projectName) {
        logger.debug('Creating project with key <{}>', projectKey)

        http.post {
            request.uri.path = '/api/projects/create'
            request.headers[AUTH_HEADER_KEY] = authHeader
            request.contentType = 'application/json'
            request.body = [
                    "project": projectKey,
                    "name": projectName
            ]
            response.success { FromServer fs, Object body ->
                logger.debug('Create project response code: {}', fs.statusCode)
                if (body && body.project.key) {
                    logger.info('Project with key {} created.', projectKey)
                    return body.project.key
                }
                throw new SonarCreateProjectFailedException(projectKey)
            }
        }
    }

}
