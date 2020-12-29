# jmeter-distributed-ocp
JMeter distributed testing POC for Open shift using IKEA OCP (ocm-02)

## Setup

This setup contains a demo of two different setups of JMeter in Openshift
- JMeter test towards remote Selenium driver 
- Web API test with JMeter distributed test (Master and slaves) <br /><br />

All necessary Jenkins configuration and building of Docker images is provided in corresponging folders.<br />
Monitoring and testresults are setup with Grafana and InfluxDB.<br />

### Requirement

- An admin account in Openshift and a local installation of Openshift (oc) CLI tool is mandatory. <br />
- It is not mandatory, but in your local environment either a Linux bash or any other tool to run Unix commands is recommended. <br />
- To setup communication to Github an SSH [accesstoken](https://confluence.build.ingka.ikea.com/display/ICPW/Using+an+accesstoken+to+access+GitHub+Enterprise+repos)
 is mandatory.<br />


## Steps to follow

### 1.Build configuration

Please goto [config folder](./config/) for more information

### 2.Docker Files

Please goto [Docker folder](./Docker/) for more information

### 3.Openshift setup

Please goto [openshift folder](./openshift/) for more information

### 4.Monitoring

Monitoring with InfluxDB and Grafana
Please goto [monitor folder](./openshift/monitor/) for more information

