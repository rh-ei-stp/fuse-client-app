# Multi-Datacenter Reference Architecture :: Fuse AMQP Client Application

A Spring Boot application written with Red Hat Fuse components that acts as an AMQP message producer and consumer.

## Building the application

Build the application:

`mvn clean install`

## Run application locally

`java -jar target/fuse-client-app-1.0-SNAPSHOT.jar`

## Overrride producer/consumer properties

The app looks for configuration in the default spring boot locations. For example,
you can create a properties file `config/application.properties`, and override the 
following producer and consumer config.

```properties
#Client producer properties
producer.router.url=amqp://localhost:5672
producer.router.user=admin
producer.router.password=admin
producer.queue.name=queue.test
producer.route.switch=true
producer.message.size.bytes=256
producer.message.period.millis=1000
producer.message.count=0

#Client consumer properties
consumer.router.url=amqp://localhost:5672
consumer.router.user=admin
consumer.router.password=admin
consumer.queue.name=queue.test
consumer.route.switch=true

```

## Creating a container image with s2i

Get the latest Fuse image streams:

`oc apply -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.fuse-730065-redhat-00002/fis-image-streams.json -n openshift`

Create a binary s2i build configuration:

`oc process -f openshift/build.yml -o yaml | oc apply -f -`

Source application artifact and run image build:

`oc start-build fuse-client-app --from-file=target/fuse-client-app-1.0-SNAPSHOT.jar --follow`

## Deploying the application image

Apply a configmap for application configuration:

`oc create configmap fuse-client-app --from-file=src/main/resources/application.properties --dry-run -o yaml | oc apply -f -`

Apply the application template:

`oc process -f openshift/application.yml -o yaml | oc apply -f -`

Rollout the latest deployment

`oc rollout latest dc/fuse-client-app`

Follow the progress of the deployment

`oc rollout status dc/fuse-client-app --watch`

## Using a Jenkins pipeline for building and deploying

Get the latest Fuse image streams:

`oc apply -f https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.fuse-730065-redhat-00002/fis-image-streams.json -n openshift`

Create a project for CI

`oc new-project ci`

Deploy a Jenkins master

`oc process jenkins-persistent MEMORY_LIMIT=2Gi ENABLE_OAUTH=false -o yaml -n openshift | oc apply -f -`

Note: I did not have luck with Jenkins OAuth in an RHPDS v4.1 cluster. Turning off OAuth means the you log into the console with username: `admin` password: `password`

Apply the application pipeline

`oc apply -f openshift/pipeline.yml`

Note: At this time the pipeline is assuming we are deploying the app to the `datacenter-a` namespace. We can target other namespaces by changing the constants in the Jenkinsfile and configuring a Role Binding for the Jenkins service account in `pipeline.yml`.

Start the pipeline build

`oc start-build fuse-client-app-pipeline`
