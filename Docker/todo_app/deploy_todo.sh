#!/usr/bin/env bash
#Create JMeter namespaces and deploy PODS with config on an existing kuberntes cluster

working_dir=$(pwd)


echo "checking if oc.exe is present"

if ! hash oc.exe 2>/dev/null
then
    echo "'oc.exe' was not found in PATH"
    echo "Kindly ensure that you can acces an existing kubernetes cluster via oc.exe"
    exit
fi

oc.exe version

echo "Deploy TODO POD "

oc.exe create -f todo_deploy.yaml
oc.exe create -f todo_svc.yaml

echo "Get all PODS "

oc.exe get po
