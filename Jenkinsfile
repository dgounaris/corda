//import static com.r3.build.BuildControl.killAllExistingBuildsForJob
//@Library('corda-shared-build-pipeline-steps')
//import static com.r3.build.BuildControl.killAllExistingBuildsForJob

//killAllExistingBuildsForJob(env.JOB_NAME, env.BUILD_NUMBER.toInteger())

pipeline {
    agent { label 'aks' }
    options {
        timestamps()
        timeout(time: 3, unit: 'HOURS')
    }

    //environment {
        //DOCKER_TAG_TO_USE = "${env.GIT_COMMIT.subSequence(0, 8)}"
        //EXECUTOR_NUMBER = "${env.EXECUTOR_NUMBER}"
        //BUILD_ID = "${env.BUILD_ID}-${env.JOB_NAME}"
        //ARTIFACTORY_CREDENTIALS = credentials('artifactory-credentials')
    //}

    stages {
        stage('Build') {
            steps {
                sh "./gradlew clean build --stacktrace"
            }
        }
        stage('Generate Sonarqube code report (no tests)') {
            steps {
                withSonarQubeEnv('sq01') {
                    sh "./gradlew --no-daemon sonarqube --stacktrace"
                }
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    script {
                        script {
                           try {
                                def qg = waitForQualityGate();
                                if (qg.status != 'OK') {
                                    error "Pipeline aborted due to quality gate failure: ${qg.status}"
                                }
                            } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
                                println('No sonarqube webhook response within timeout. Please check the webhook configuration in sonarqube.')
                                // continue the pipeline
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
         //   archiveArtifacts artifacts: '**/pod-logs/**/*.log', fingerprint: false
            junit '**/build/test-results-xml/**/*.xml'
        }
        cleanup {
            deleteDir() /* clean up our workspace */
        }
    }
}
