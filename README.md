# Fuse AMQP Client Application

A Spring Boot application written with Red Hat Fuse components that acts as an AMQP message producer and consumer.

## Building the application

Build the application:

`mvn clean install`

## Creating a container image with s2i

Get the latest Fuse image streams:

`oc apply -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-790035/fis-image-streams.json -n openshift`

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

## AMQ Broker
* TODO this is not working.
* Install AMQ Broker
* apply resources in amq-broker

## Install Keycloak
https://www.keycloak.org/getting-started/getting-started-openshift

`oc process -f https://raw.githubusercontent.com/keycloak/keycloak-quickstarts/latest/openshift-examples/keycloak.yaml \
    -p KEYCLOAK_USER=admin \
    -p KEYCLOAK_PASSWORD=admin \
    -p NAMESPACE=keycloak \
| oc create -f - `

Create and Import Realm. Name "Myrealm"

Create user
username: user
Password: password

https://keycloak-keycloak.apps-crc.testing/auth/realms/myrealm/.well-known/openid-configuration
https://www.keycloak.org/getting-started/getting-started-openshift

## Keycloak Client
https://www.keycloak.org/docs/latest/securing_apps/#_spring_boot_adapter

myclient oauth client info
grant type: authorization code
clientId: myclient
auth url: 
https://keycloak-keycloak.apps-crc.testing/auth/realms/myrealm/protocol/openid-connect/auth

access token url:
https://keycloak-keycloak.apps-crc.testing/auth/realms/myrealm/protocol/openid-connect/token

## Apply AMQ Client Service and Route

`oc apply -f openshift/amq-client-service.yml`
`oc apply -f openshift/amq-client-route.yml`

## Using a Jenkins pipeline for building and deploying

Get the latest Fuse image streams:

`oc apply -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-790035/fis-image-streams.json -n openshift`

Apply the application pipeline

`oc apply -f openshift/pipeline.yml`

Start the pipeline build

`oc start-build fuse-amq-client-pipeline`

## Known Issues
* Requires Java 8 without command line options: https://stackoverflow.com/questions/43574426/how-to-resolve-java-lang-noclassdeffounderror-javax-xml-bind-jaxbexception
