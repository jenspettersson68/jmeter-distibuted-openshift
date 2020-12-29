pipeline {
    agent any
        environment {
            //JMeter TestCase
            TESTCASE = 'springboot2.jmx'
            //namespace to use
            NAMESPACE = 'sandbox-continuous-testing' 
            env_cfgmap = 'env-variables'
            csv_cfgmap = 'csv-data'
            //configmap 'jmeter-load-test' name defined in YAML file
            jmeter_cfgmap = 'jmeter-load-test'
            //JMeter service
            jmeter_svc = 'jmeter-slaves-svc'
        }
    stages {

        stage('Checkout to Master') {
            steps {
                script{
                    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'default-ssh-key', url: 'ssh://git@git.build.ingka.ikea.com/continuous-testing/jmeter-distributed-ocp.git']]])
                }
            }
        }

        stage('Setup environment') {
            steps {
                script{
                    openshift.withCluster() {
                        openshift.withProject(env.NAMESPACE) {
                            //Check for resources in namespace (project) and delete them
                            if (openshift.selector("configmap", env.csv_cfgmap).exists()) { 
                                openshift.selector("configmap", env.csv_cfgmap).delete()
                            }
                            if (openshift.selector("configmap", env.env_cfgmap).exists()) { 
                                openshift.selector("configmap", env.env_cfgmap).delete()
                            }
                            if (openshift.selector("configmap", env.jmeter_cfgmap).exists()) { 
                                openshift.selector("configmap", env.jmeter_cfgmap).delete()
                            }
                            if (openshift.selector("deployment", "jmeter-master").exists()) { 
                                openshift.selector("deployment", "jmeter-master").delete()
                                def time = '10'
                                sleep time.toInteger()
                            }
                            if (openshift.selector("deployment", "jmeter-slaves").exists()) { 
                                openshift.selector("deployment", "jmeter-slaves").delete()
                                def time = '20'
                                sleep time.toInteger()
                            }
                            if (openshift.selector("svc", "jmeter-slaves-svc").exists()) { 
                                openshift.selector("svc", "jmeter-slaves-svc").delete()
                                def time = '10'
                                sleep time.toInteger()
                            }
                            //Creating and deploying configmaps
                            //create configmap from YAML file
                            openshift.create( readFile( 'openshift/jmeter_master_configmap.yaml' ) )
                            //Create environment variable config map"
                            openshift.create( 'configmap', env.env_cfgmap , '--from-env-file=openshift/variables.env' )
                            //Deploy configmap for test execution
                            openshift.create( 'configmap', env.csv_cfgmap , '--from-file=openshift/test.csv' )
                            def time = '5'
                            sleep time.toInteger()

                            //Deploy JMeter Pods from YAML

                            openshift.create( readFile( 'openshift/jmeter_master_deploy.yaml' ) )
                            openshift.create( readFile( 'openshift/jmeter_slaves_deploy.yaml' ) )
                            time = '10'
                            sleep time.toInteger()
                            openshift.create( readFile( 'openshift/jmeter_slaves_svc.yaml' ) )

                        sh '''
                            set -x
                            echo "Printout Of the $NAMESPACE Objects"

                            #echo

                            oc get -n $NAMESPACE all
                            '''
                        }
                    }
                }
            }
        }

        stage('Run tests') {
            steps {
                script{
                    openshift.withCluster() {
                        openshift.withProject(env.NAMESPACE) {
                        echo "Hello from project ${openshift.project()} in cluster ${openshift.cluster()}"
                        def time = '120'
                        echo "Waiting ${time} seconds for deployment to complete prior starting smoke testing"
                        sleep time.toInteger()
                        def connected = openshift.verifyService(env.jmeter_svc)
    
                        if (connected) {
                            echo "Able to connect to ${jmeter_svc}"

                            sh '''
                                set -x
                                master_pod=`oc get po -n $NAMESPACE | grep jmeter-master | awk '{print $1}'`
                                echo $master_pod
                                ls -la
                                
                                oc exec -i -n $NAMESPACE $master_pod -- bash -c 'rm -rf /testdata/report/'
                                oc exec -i -n $NAMESPACE $master_pod -- bash -c 'rm -f /testdata/results.jtl'

                                oc cp openshift/$TESTCASE -n $NAMESPACE $master_pod:/testdata/$TESTCASE

                                oc exec -i -n $NAMESPACE $master_pod bash /testdata/loadtest/load_test $TESTCASE

                                timestamp=$(date +'%Y%m%d_%H%M%S')
                                oc cp -n $NAMESPACE $master_pod:/testdata/report ./report_$timestamp

                                '''
                        } else {
                            echo "Unable to connect to ${jmeter_svc}"
                            }
                        }
                    }
                }
            }
        }

        stage('Tear down environment') {
            steps {
                script{
                    openshift.withCluster() {
                        openshift.withProject(env.NAMESPACE) {
                            if (openshift.selector("configmap", env.csv_cfgmap).exists()) { 
                                openshift.selector("configmap", env.csv_cfgmap).delete()
                            }
                            if (openshift.selector("configmap", env.env_cfgmap).exists()) { 
                                openshift.selector("configmap", env.env_cfgmap).delete()
                            }
                            if (openshift.selector("configmap", env.jmeter_cfgmap).exists()) { 
                                openshift.selector("configmap", env.jmeter_cfgmap).delete()
                            }
                            if (openshift.selector("deployment", "jmeter-master").exists()) { 
                                openshift.selector("deployment", "jmeter-master").delete()
                            }
                            if (openshift.selector("deployment", "jmeter-slaves").exists()) { 
                                openshift.selector("deployment", "jmeter-slaves").delete()
                            }
                            if (openshift.selector("svc", "jmeter-slaves-svc").exists()) { 
                                openshift.selector("svc", "jmeter-slaves-svc").delete()
                            }
                        }
                    }
                }
            }
        }
    }
}