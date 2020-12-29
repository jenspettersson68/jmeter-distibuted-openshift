#!/bin/bash
# Build Docker images first before psuhing to registry
# SET VARIABLE $PROJECT to your project!!
# Run script ./push_images.sh

docker push docker-registry-default.ocp-02.ikeadt.com/$PROJECT/jmeter-master-ikea:latest
docker push docker-registry-default.ocp-02.ikeadt.com/$PROJECT/jmeter-slave-ikea:latest