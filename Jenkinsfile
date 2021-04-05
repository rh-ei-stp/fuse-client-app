#!/usr/bin/groovy

node('maven') {

    // you may want to define build and deployment namespaces separately and use the appropriate variables in various stages
    env.NAMESPACE           = "amq-client"
    env.APP_NAME            = "fuse-amq-client"
    env.API_VERSION         = "1.0"
    env.DEPLOYMENT_VERSION  = "v${API_VERSION}.${BUILD_NUMBER}"

    currentBuild.displayName = "${DEPLOYMENT_VERSION}"
    currentBuild.description = "${APP_NAME}-${API_VERSION}.${BUILD_NUMBER}"

    checkout scm

    stage('Build') {
        sh "mvn -B clean install -DskipTests=true --settings configuration/settings.xml"
    }

    stage('Unit Test') {
        sh "mvn -B test --settings configuration/settings.xml"
    }

    stage('Build Image') {
        def oc = "oc -n ${NAMESPACE}"
        sh "${oc} process -f openshift/build.yml -p APPLICATION_IMAGE_TAG=${DEPLOYMENT_VERSION} | ${oc} apply -f -"
        sh "rm -rf oc-build && mkdir -p oc-build/deployments"
        sh "cp -rf target/*.jar oc-build/deployments/"
        sh "${oc} start-build ${APP_NAME} --from-dir=oc-build --wait --follow"
    }

    stage('Deploy to DEV') {
        def oc = "oc -n ${NAMESPACE}"
        sh "${oc} create configmap fuse-amq-client --from-file=src/main/resources/application.properties --dry-run=client -o yaml | ${oc} apply -f -"
        sh "${oc} process -f openshift/application.yml -p APPLICATION_IMAGE_TAG=${DEPLOYMENT_VERSION} | ${oc} apply -f -"
        // use an image stream tagging command if promoting from a build namespace to a deployment namespace
        // sh "${oc} tag ${CI_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION} ${DEV_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION}"
        sh "${oc} rollout latest dc/${APP_NAME}"
        sh "${oc} rollout status dc/${APP_NAME} --watch"
    }

}
