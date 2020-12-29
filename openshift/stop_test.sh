#!/usr/bin/env bash
#Script writtent to stop a running jmeter master test
#Kindly ensure you have the necessary kubeconfig
namespace=sandbox-continuous-testing

working_dir=`pwd`

#Get namesapce variable
#tenant=`awk '{print $NF}' $working_dir/tenant_export`
#tenant=jmeter

master_pod=`oc.exe get po -n $namespace | grep jmeter-master | awk '{print $1}'`

oc.exe exec -ti -n $namespace $master_pod -- bash -c '$JMETER_HOME/bin/stoptest.sh'
