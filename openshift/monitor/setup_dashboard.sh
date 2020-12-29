#!/usr/bin/env bash

working_dir=`pwd`

#Get namesapce variable
NAMESPACE='sandbox-continuous-testing'
DASHBOARD='historic.json'
grafana_svc='jmeter-grafana'

##Wait until Influxdb Deployment is up and running
##influxdb_status=`oc.exe get po -n $tenant | grep influxdb-jmeter | awk '{print $2}' | grep Running

## Create jmeter database automatically in Influxdb
EXTERNAL_IP=`oc.exe get svc | grep jmeter-grafana | awk '{print $4}'`

echo "Creating Influxdb jmeter Database"

influxdb_pod=`oc.exe get po | grep influxdb-jmeter | awk '{print $1}'`
oc.exe exec -ti $influxdb_pod -- influx -execute 'CREATE DATABASE jmeter'

echo "Creating the Grafana data source"
DATASRC_URL=`echo http://admin:admin@$grafana_svc-$NAMESPACE.ocp-02.ikeadt.com/api/datasources`

curl $DATASRC_URL -X POST -H 'Content-Type: application/json;charset=UTF-8' \
--data-binary '{"name":"jmeterdb","type":"influxdb","typeLogoUrl":"public/app/plugins/datasource/influxdb/img/influxdb_logo.svg", "url":"http://jmeter-influxdb:8086","access":"proxy","isDefault":true,"database":"jmeter","user":"admin","password":"admin","tlsSkipVerify":true,"version":"InfluxQL","readOnly":false}'
echo
sleep 5
echo
echo "Creating the Grafana Dashboard"
echo
DASHBOARD_URL=`echo http://admin:admin@$grafana_svc-$NAMESPACE.ocp-02.ikeadt.com/api/dashboards/import`

curl -X POST -i $DASHBOARD_URL -d "{\"dashboard\":$(cat ./$DASHBOARD)}" -H 'Content-Type: application/json;charset=UTF-8'

sleep 2
echo
URL=`echo http://$grafana_svc-$NAMESPACE.ocp-02.ikeadt.com/login`
echo 'Grafana Dashboard : ' $URL