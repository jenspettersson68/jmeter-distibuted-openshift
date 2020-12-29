#!/usr/bin/env bash
#Script created to launch Jmeter tests directly from the current terminal without accessing the jmeter master pod.
#It requires jmx file in same directory
#After execution, test script jmx file may be deleted from the pod itself but not locally.
#Run script with ./run_test.sh <jmx-file>

# Get the namespace from file

namespace=sandbox-continuous-testing

#Get Master pod details

master_pod=`oc get po -n $namespace | grep jmeter-master | awk '{print $1}'`

#Cleaning up if running more than one test
oc exec -ti -n $namespace $master_pod -- bash -c 'rm -rf /testdata/report/'
oc exec -ti -n $namespace $master_pod -- bash -c 'rm -f /testdata/results.jtl'
#Copy JMX file to pod
#oc.exe cp $1 $master_pod:$1
oc cp $1 -n $namespace $master_pod:/testdata/$1


## Echo Starting Jmeter load test

oc exec -ti -n $namespace $master_pod bash /testdata/loadtest/load_test $1
#Copy report to host
timestamp=$(date +'%Y%m%d_%H%M%S')
oc cp -n $namespace $master_pod:testdata/report ./report_$timestamp


