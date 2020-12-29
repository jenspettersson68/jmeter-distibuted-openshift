# Open-shift

This folder contains all setup for creating test environment and all correspoging files for running the JMeter tests.<br />

## Test setup

The buildconfig jobs in Jenkins use the YAML files in this folder to setup the test environment.<br />
The distributed tests using the jmeter master yaml files to setup the master with corresponding config and jmeter slaves to deploy and start the pods as loadgenerators.

### Test files

For the seleniumtests we are testing ikea production site (__ikea_test.jmx__) which are only using two threads due to we don't want to have any extra load in production.<br />
We have two test files for the springboot application, __springboot.jmx__ for the standard backend listener and __springboot2.jmx__ for the JMeter-InfluxDB-Writer plogin.<br />
Varaibles and shell script for the tests is created by configmaps deployment.<br />
All tests can (and should) be executed in Jenkins).<br />
The shell script for running tests should only be used for testing. The stop_test.sh can be used for killing tests remotely.<br />
IMPORTANT: In jmeter slaves YAML files, the variable, replicas control the number of slaves to deploy. Please be carefull of the boundaries in openshift cluster.

#### Deploy manually

oc.exe create -f jmeter_master_configmap.yaml<br />
oc.exe create configmap env-variables --from-env-file=./variables.env<br />
oc.exe create configmap csv-data --from-file=./test.csv<br />
oc.exe create -f jmeter_master_deploy.yaml<br />
oc.exe create -f jmeter_slaves_deploy.yaml<br />
oc.exe create -f jmeter_slaves_svc.yaml<br /><br />
Selenium tests: <br />
First build the JMeter maste image for Selenium and deploy the JMeter master image<br />
oc.exe create -f jmeter_master_deploy.yaml<br />
Selenium setup: <br />
oc.exe create -f selenium_deploy.yaml<br />
oc.exe create -f selenium_svc.yaml<br />


#### Files and folder reference: 
- __jmeter_master_deploy.yaml__ : *YAML config for deploying JMeter master pod*
- __jmeter_master_configmap.yaml__ : *YAML config for create and deploy jmeter commands*
- __jmeter_master_selenium_configmap.yaml__ : *YAML config for create and deploy jmeter commands for Selenium tests*
- __jmeter_slaves_deploy.yaml__ : *YAML config for deploying JMeter slave pods (number of replicas set number of slaves)*
- __jmeter_slaves_svc.yaml__ : *YAML config for starting JMeter slave services*
- __selenium_deploy.yaml__ : *YAML config for for deploying Seleniumstandalone server*
- __selenium_svc.yaml__ : *YAML config for starting Selenium service*
- __test.csv__ : *Example configmap input for parameters*
- __variables.env__ : *Example configmap input for system env. variables*
- __springboot.jmx__ : *JMeter testcase for springboot application*
- __ikea_test.jmx__ : *JMeter testcase for Selenium test*
- __stop_test.sh__ : *Killing test from local environment*
- __run_test.sh__ : *Example script for running tests from local environment*
- __monitor__ : *Folder for setting up influxDB and Grafana*