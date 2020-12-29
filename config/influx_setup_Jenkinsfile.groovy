pipeline {
    agent any
        environment {
            //namespace to use
            NAMESPACE = 'sandbox-continuous-testing'
            DASHBOARD = 'jmeter_load_test.json'
            influx_cfgmap = 'influxdb-config'
            //JMeter service
            grafana_svc = 'jmeter-grafana'
            influx_svc = 'jmeter-influxdb'
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
                            if (openshift.selector("routes", "jmeter-grafana").exists()) { 
                                openshift.selector("routes", "jmeter-grafana").delete()
                            }
                            if (!openshift.selector("configmap", env.influx_cfgmap).exists()) { 
                                openshift.create( readFile( 'openshift/monitor/jmeter_influxdb_configmap.yaml' ) )
                            }
                            if (!openshift.selector("deployment", "jmeter-grafana").exists()) { 
                                openshift.create( readFile( 'openshift/monitor/jmeter_grafana_deploy.yaml' ) )
                                def time = '20'
                                sleep time.toInteger()
                            }
                            if (!openshift.selector("svc", "jmeter-grafana").exists()) { 
                                openshift.create( readFile( 'openshift/monitor/jmeter_grafana_svc.yaml' ) )
                                def time = '10'
                                sleep time.toInteger()

                            }
                            if (!openshift.selector("deployment", "influxdb-jmeter").exists()) { 
                                openshift.create( readFile( 'openshift/monitor/jmeter_influxdb_deploy.yaml' ) )
                                def time = '20'
                                sleep time.toInteger()
                            }
                            if (!openshift.selector("svc", "jmeter-influxdb").exists()) { 
                                openshift.create( readFile( 'openshift/monitor/jmeter_influxdb_svc.yaml' ) )
                                def time = '10'
                                sleep time.toInteger()
                            }

                        sh '''
                            set -x
                            oc expose svc -n $NAMESPACE $grafana_svc
                            echo "Printout Of the $NAMESPACE Objects"
                            #echo
                            oc get -n $NAMESPACE all
                            '''
                        }
                    }
                }
            }
        }

        stage('Setup Dashboard') {
            steps {
                script{
                    openshift.withCluster() {
                        openshift.withProject(env.NAMESPACE) {
                        echo "Hello from project ${openshift.project()} in cluster ${openshift.cluster()}"
                        def time = '30'
                        echo "Waiting ${time} seconds for deployment to complete prior starting smoke testing"
                        sleep time.toInteger()

                        sh '''
                            set -x

                            echo "Creating Influxdb jmeter Database"
                            influxdb_pod=`oc get po -n $NAMESPACE | grep influxdb-jmeter | awk '{print $1}'`

                            echo "Creating the Grafana data source"
                            DATASRC_URL=`echo http://admin:admin@$grafana_svc-$NAMESPACE.ocp-02.ikeadt.com/api/datasources`

                            curl $DATASRC_URL -X POST -H 'Content-Type: application/json;charset=UTF-8' \
                            --data-binary '{"name":"jmeterdb","type":"influxdb","typeLogoUrl":"public/app/plugins/datasource/influxdb/img/influxdb_logo.svg", \
                            "url":"http://jmeter-influxdb:8086","access":"proxy","isDefault":true,"database":"jmeter","user":"admin","password":"admin", \
                            "tlsSkipVerify":true,"version":"InfluxQL","readOnly":false}'

                            echo "Creating the Grafana Dashboard"
                            DASHBOARD_URL=`echo http://admin:admin@$grafana_svc-$NAMESPACE.ocp-02.ikeadt.com/api/dashboards/import`

                            curl -X POST -i $DASHBOARD_URL -d "$(cat openshift/monitor/$DASHBOARD)" -H 'Content-Type: application/json;charset=UTF-8'
                            URL=`echo http://$grafana_svc-$NAMESPACE.ocp-02.ikeadt.com/login`
                            echo 'Grafana Dashboard : ' $URL

                            '''
                        }
                    }
                }
            }
        }
    }
}