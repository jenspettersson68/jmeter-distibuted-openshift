#!/bin/bash
# Tag the ikea Base image 
# SET VARIABLE $PROJECT to your project!!
docker tag ikea/jmeter-base-ikea:latest docker-registry-default.ocp-02.ikeadt.com/$PROJECT/jmeter-base-ikea:latest
