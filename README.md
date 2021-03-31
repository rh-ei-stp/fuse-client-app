# Fuse AMQP Client Application

A Spring Boot application written with Red Hat Fuse components that acts as an AMQP message producer and consumer.

## Building the application

Build the application:

`mvn clean install`

## Creating a container image with s2i

Get the latest Fuse image streams:

`oc apply -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-790030/fis-image-streams.json -n openshift`

Create a binary s2i build configuration:

`oc process -f openshift/build.yml -o yaml | oc apply -f -`

Source application artifact and run image build:

`oc start-build fuse-amq-client --from-file=target/fuse-amq-client-1.0-SNAPSHOT.jar --follow`

## Deploying the application image

Apply a configmap for application configuration:

`oc create configmap fuse-amq-client --from-file=src/main/resources/application.properties --dry-run=client -o yaml | oc apply -f -`

Apply the application template:

`oc process -f openshift/application.yml -o yaml | oc apply -f -`

Rollout the latest deployment

`oc rollout latest dc/fuse-amq-client`

Follow the progress of the deployment

`oc rollout status dc/fuse-amq-client --watch`

## Using a Jenkins pipeline for building and deploying

Get the latest Fuse image streams:

`oc apply -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-790030/fis-image-streams.json -n openshift`

Apply the application pipeline

`oc apply -f openshift/pipeline.yml`

Start the pipeline build

`oc start-build fuse-amq-client-pipeline`

