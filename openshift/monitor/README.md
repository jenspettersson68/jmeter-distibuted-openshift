
# Setup of environment
The containers used in this setup is picked up from RedHar registry and just started in the project to create the imagestreams.

## InfluxDB setup
We are creating a persistent storage where we create the JMeter influx DB database(jmeter_influxdb_volumeclaim.yaml). All setting for the communication and setup for influx is deployed by the configmap (jmeter_influxdb_configmap.yaml). We will deploy the influxDB pod and start it as a service <br />
In the setup of dashboard script we will create the database
## Grafana setup
We deploy Grafana as pod and start the service by the YAML-files. We expose the service to be able to get a readable URL<br />
In the setup dashboard we are creating datasource for the influxDB and deploing an example dashboard (jmeter_load_test.json).<br />
The address to grafana is provided by the script. Login is the default values admin/admin which is recommended to change.<br /><br />
Dashboard :<br />
![Dashboard view:](./pics/grafana_full.jpg)<br /><br />
Datasource :<br />
![Data source:](./pics/datasource.JPG)<br />

## File list 
Files: <br />
- __jmeter_grafana_deploy.yaml__ : *Deploy Grafana*
- __jmeter_grafana_svc.yaml__ : *Start Grafana as services*
- __jmeter_influxdb_configmap.yaml__ : *Script for creating setting in influxDB*
- __jmeter_influxdb_volumeclaim.yaml__ : *Create persistent volume for JMeter DB*
- __jmeter_influxdb_deploy.yaml__ : *Deploy InfluxDB pod*
- __jmeter_influxdb_svc.yaml__ : *Start InfluxDB as service*
- __jmeter_load_test.json__ : *Example of Grafana dashboard json configuration and setup*
- __setup_dashboard.sh__ : *Script to setup database in InfluxDB and datasource/dashboard in Grafana*