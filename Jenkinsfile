pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        JAVA_HOME = 'C:\\Users\\nipad\\.jdks\\ms-21.0.13.1'

        SONARQUBE_SERVER = 'SonarQubeServer'
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'nipa93/bmi_3013_delara'
        DOCKER_IMAGE_TAG = 'latest'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/NipaDelara/SEP2_BMI_SonarQube_Jenkins.git'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean verify'
            }
        }

        stage('JaCoCo Coverage') {
            steps {
                bat 'if not exist target\\site\\jacoco\\jacoco.xml exit /b 1'

                junit allowEmptyResults: false,
                      testResults: 'target/surefire-reports/*.xml'

                archiveArtifacts artifacts: 'target/site/jacoco/**',
                                 allowEmptyArchive: false
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    withCredentials([
                        string(
                            credentialsId: 'SONAR_TOKEN',
                            variable: 'SONAR_TOKEN'
                        )
                    ]) {
                        bat '''
                            sonar-scanner ^
                            -Dsonar.projectKey=BMI_CL ^
                            -Dsonar.projectName=BMI_CL ^
                            -Dsonar.sources=src/main/java ^
                            -Dsonar.tests=src/test/java ^
                            -Dsonar.java.binaries=target/classes ^
                            -Dsonar.java.test.binaries=target/test-classes ^
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml ^
                            -Dsonar.token=%SONAR_TOKEN%
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Check Dockerfile') {
            steps {
                bat 'dir Dockerfile'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build(
                        "${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}"
                    )
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry(
                        'https://index.docker.io/v1/',
                        DOCKERHUB_CREDENTIALS_ID
                    ) {
                        docker.image(
                            "${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}"
                        ).push()
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    docker rm -f bmi-app 2>NUL
                    docker run -d --name bmi-app --restart unless-stopped -e DISPLAY=host.docker.internal:0.0 nipa93/bmi_3013_delara:latest
                '''
            }
        }
    }

    post {
        success {
            echo 'BMI Jenkins pipeline completed successfully!'
        }

        failure {
            echo 'BMI Jenkins pipeline failed. Check console output.'
        }
    }
}
