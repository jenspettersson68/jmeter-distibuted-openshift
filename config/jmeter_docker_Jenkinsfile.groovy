pipeline {
    //If running it as Cron Job
    //triggers { pollSCM '* * * * *' }    
    agent any
    
    environment {
        NAMESPACE = 'sandbox-continuous-testing' 
        // DBuildconfigs
        BC_BASE = 'jmeter-base-ikea'
        BC_MASTER = 'jmeter-master-ikea'
        BC_SLAVE = 'jmeter-slave-ikea'
        DOCKER_FILE_BASE = 'Dockerfile-base-redhat'
        DOCKER_FILE_MASTER = 'Dockerfile-master'
        DOCKER_FILE_SLAVE = 'Dockerfile-slave'

    }

    options {
        timeout(time: 20, unit: 'MINUTES') 
    }

    stages {

        stage('Checkout') {
            steps {
                script{
                    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'default-ssh-key', url: 'ssh://git@git.build.ingka.ikea.com/continuous-testing/jmeter-distributed-ocp.git']]])
                }
            }
        }

        stage('Create BASE Imagestream Buildconfig') {
            steps {
                echo 'Creating buildonfigs'
                    script {
                            openshift.withCluster() {
                                openshift.withProject(env.NAMESPACE) {
                                if (openshift.selector("bc", env.BC_BASE).exists()) { 
                                    openshift.selector("bc", env.BC_BASE).delete()
                                }
                            }

                        sh '''
                            set -x
                            cat Docker/$DOCKER_FILE_BASE | oc new-build -D - -n $NAMESPACE --name $BC_BASE
                            echo "Buildconfig $BC_BASE is created"
                            '''
                    }
                }
            }
        }

        stage('Build and push BASE Imagestream to Openshift') {
            steps {
                echo 'Building and pushing Imagestream'
                    script {
                        openshift.withCluster() {
                            openshift.withProject(env.NAMESPACE) {
                            def builds = openshift.selector("bc", env.BC_BASE).related('builds')
                                timeout(15) { 
                                    builds.untilEach(1) {
                                    return (it.object().status.phase == "Complete")    
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Create MASTER Imagestream Buildconfig') {
            steps {
                echo 'Creating buildonfigs'
                    script {
                            openshift.withCluster() {
                                openshift.withProject(env.NAMESPACE) {
                                if (openshift.selector("bc", env.BC_MASTER).exists()) { 
                                    openshift.selector("bc", env.BC_MASTER).delete()
                                }
                            }

                        sh '''
                            set -x
                            cat Docker/$DOCKER_FILE_MASTER | oc new-build -D - -n $NAMESPACE --name $BC_MASTER
                            echo "Buildconfig $BC_MASTER is created"
                            '''
                    }
                }
            }
        }

        stage('Build and push MASTER Imagestream to Openshift') {
            steps {
                echo 'Building and pushing Imagestream'
                    script {
                        openshift.withCluster() {
                            openshift.withProject(env.NAMESPACE) {
                            def builds = openshift.selector("bc", env.BC_MASTER).related('builds')
                                timeout(5) { 
                                    builds.untilEach(1) {
                                    return (it.object().status.phase == "Complete")    
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Create SLAVE Imagestream Buildconfig') {
            steps {
                echo 'Creating buildonfigs'
                    script {
                            openshift.withCluster() {
                                openshift.withProject(env.NAMESPACE) {
                                if (openshift.selector("bc", env.BC_SLAVE).exists()) { 
                                    openshift.selector("bc", env.BC_SLAVE).delete()
                                }
                            }

                        sh '''
                            set -x
                            cat Docker/$DOCKER_FILE_SLAVE | oc new-build -D - -n $NAMESPACE --name $BC_SLAVE
                            echo "Buildconfig $BC_SLAVE is created"
                            '''
                    }
                }
            }
        }

        stage('Build and push SLAVE Imagestream to Openshift') {
            steps {
                echo 'Building and pushing Imagestream'
                    script {
                        openshift.withCluster() {
                            openshift.withProject(env.NAMESPACE) {
                            def builds = openshift.selector("bc", env.BC_SLAVE).related('builds')
                                timeout(5) { 
                                    builds.untilEach(1) {
                                    return (it.object().status.phase == "Complete")    
                                }
                            }
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
                                if (openshift.selector("bc", env.BC_BASE).exists()) { 
                                    openshift.selector("bc", env.BC_BASE).delete()
                                }
                            }
                                if (openshift.selector("bc", env.BC_MASTER).exists()) { 
                                    openshift.selector("bc", env.BC_MASTER).delete()
                                }
                                if (openshift.selector("bc", env.BC_SLAVE).exists()) { 
                                    openshift.selector("bc", env.BC_SLAVE).delete()
                                }

                           sh '''

                            set -x
                            echo 'Deleting Build PODS'

                            base_build_pod=`oc get po -n $NAMESPACE | grep $BC_BASE | awk '{print $1}'`
                            echo $base_build_pod
                            if [ $base_build_pod != "" ] 
                            then
                                oc delete -n $NAMESPACE pod $base_build_pod
                            fi

                            master_build_pod=`oc get po -n $NAMESPACE | grep $BC_MASTER | awk '{print $1}'`
                            echo $master_build_pod
                            if [ $master_build_pod != "" ] 
                            then
                                oc delete -n $NAMESPACE pod $master_build_pod
                            fi

                            slave_build_pod=`oc get po -n $NAMESPACE | grep $BC_SLAVE | awk '{print $1}'`
                            echo $slave_build_pod
                            if [ $slave_build_pod != "" ] 
                            then
                                oc delete -n $NAMESPACE pod $slave_build_pod
                            fi

                            '''
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
