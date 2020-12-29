pipeline {
    agent any
        environment {
            //namespace to use
            NAMESPACE = 'sandbox-continuous-testing' 
            influx_cfgmap = 'influxdb-config'
            //JMeter service
            grafana_svc = 'jmeter-grafana'
            influx_svc = 'jmeter-influxdb'
            influx_dp = 'influxdb-jmeter'
        }
    stages {

        stage('Checkout to Master') {
            steps {
                script{
                    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'default-ssh-key', url: 'ssh://git@git.build.ingka.ikea.com/continuous-testing/jmeter-distributed-ocp.git']]])
                }
            }
        }

        stage('Deleting all Influx and Grafana PODS') {
            steps {
                script{
                    openshift.withCluster() {
                        openshift.withProject(env.NAMESPACE) {
                            //Check for resources in namespace (project) and delete them
                            if (openshift.selector("routes", env.grafana_svc).exists()) { 
                                openshift.selector("routes", env.grafana_svc).delete()
                            }
                            if (openshift.selector("configmap", env.influx_cfgmap).exists()) { 
                                openshift.selector("configmap", env.influx_cfgmap).delete() 
                            }
                            if (openshift.selector("deployment", env.grafana_svc).exists()) {
                                openshift.selector("deployment", env.grafana_svc).delete() 
                            }
                            if (openshift.selector("svc", env.grafana_svc).exists()) { 
                                openshift.selector("svc", env.grafana_svc).delete() 
                            }
                            if (openshift.selector("deployment", env.influx_dp).exists()) { 
                                openshift.selector("deployment", env.influx_dp).delete()
                            }
                            if (openshift.selector("svc", env.influx_svc).exists()) { 
                                openshift.selector("svc", env.influx_svc).delete()
                            }

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
    }
}