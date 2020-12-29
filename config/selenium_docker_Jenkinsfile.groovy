pipeline {
    //If running it as Cron Job
    //triggers { pollSCM '* * * * *' }    
    agent any
    
    environment {
        NAMESPACE = 'sandbox-continuous-testing' 
        // DBuildconfigs
        //Different input, but using same master as in other tests
        //This needs to be changed if modifying to much
        BC_MASTER = 'jmeter-master-ikea'
        DOCKER_FILE_MASTER = 'Dockerfile-master-selenium'


    }

    options {
        timeout(time: 20, unit: 'MINUTES') 
    }

    stages {

        stage('Checkout') {
            steps {
                script{
                    checkout([$class: 'GitSCM', 
                    branches: [[name: '*/master']], 
                    doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'myapp']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'default-ssh-key', url: 'ssh://git@git.build.ingka.ikea.com/continuous-testing/jmeter-distributed-ocp.git']]])
                }
            }
        }

        stage('Create MASTER Imagestream Buildconfig') {
            steps {
                echo 'Creating buildonfigs'
                    dir('myapp') {
                    script {
                            openshift.withCluster() {
                                openshift.withProject(env.NAMESPACE) {
                                if (openshift.selector("bc", env.BC_MASTER).exists()) { 
                                    openshift.selector("bc", env.BC_MASTER).delete()
                                }
                            }

                        sh '''
                            set -x
                            ls -la
                            pwd
                            cat Docker/$DOCKER_FILE_MASTER | oc new-build -D - -n $NAMESPACE --name $BC_MASTER
                            echo "Buildconfig $BC_MASTER is created"
                            oc start-build -n $NAMESPACE $BC_MASTER --from-dir . --follow
                            '''
                        }
                    }
                }
            }
        }

        stage('Clean up in Openshift') {
            steps {
                echo 'Deleting BuildConfigs'
                    script {
                        openshift.withCluster() {
                            openshift.withProject(env.NAMESPACE) {
                            if (openshift.selector("bc", env.BC_MASTER).exists()) { 
                                 openshift.selector("bc", env.BC_MASTER).delete()
                            }
                        }
                    }
                }
            }
        }

        stage('Trigger downstream builds') {
            steps {
                script {
                    echo 'Below line could Trigger a downstream build JMeter test'
                    // build job: 'ref-backend-jmeter', wait: false                                     
                }
            } 
        }
    }
}
