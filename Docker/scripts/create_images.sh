#!/bin/bash

#SET VARIABLE $PROJECT to your project!!
PROJECT=sandbox-continuous-testing
# Build images
# Run script ./create_images.sh 
# This image is built locally due to connectivities to internet
docker build -t docker-registry-default.ocp-02.ikeadt.com/$PROJECT/jmeter-base-ikea:latest -f Dockerfile-base-centos .
#This two images use base image as input and can be built both locally and in open shift
docker build -t docker-registry-default.ocp-02.ikeadt.com/$PROJECT/jmeter-master-ikea:latest -f Dockerfile-master .
docker build -t docker-registry-default.ocp-02.ikeadt.com/$PROJECT/jmeter-slave-ikea:latest -f Dockerfile-slave .


