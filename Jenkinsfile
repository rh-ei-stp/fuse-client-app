#!/usr/bin/groovy

node('maven') {

    env.CI_PROJECT          = "ci"
    env.DEV_PROJECT         = "datacenter-a" // eventually change to "datacenter-a-dev"
    //env.TEST_PROJECT        = "datacenter-a-test"
    //env.PROD_PROJECT        = "datacenter-a"
    env.APP_NAME            = "fuse-client-app"
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
        def oc = "oc --namespace=${CI_PROJECT}"
        sh "${oc} process -f openshift/build.yml -p APPLICATION_IMAGE_TAG=${DEPLOYMENT_VERSION} | ${oc} apply -f -"
        sh "rm -rf oc-build && mkdir -p oc-build/deployments"
        sh "cp -rf target/*.jar oc-build/deployments/"
        sh "${oc} start-build ${APP_NAME} --from-dir=oc-build --wait --follow"
    }

    stage('Deploy to DEV') {
        def oc = "oc --namespace=${DEV_PROJECT}"
        sh "${oc} create configmap fuse-client-app --from-file=src/main/resources/application.properties --dry-run -o yaml | ${oc} apply -f -"
        sh "${oc} process -f openshift/application.yml -p APPLICATION_IMAGE_TAG=${DEPLOYMENT_VERSION} | ${oc} apply -f -"
        sh "${oc} tag ${CI_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION} ${DEV_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION}"
        sh "${oc} rollout latest dc/${APP_NAME}"
        sh "${oc} rollout status dc/${APP_NAME} --watch"
    }

}

/*
stage('Approve Promotion') {
    milestone 1
    input 'Proceed to TEST?'
    milestone 2
}

node('maven') {

    checkout scm

    dir(APP_NAME) {

        stage('Deploy to TEST') {
            def oc = "oc --namespace=${TEST_PROJECT}"
            sh "${oc} process -f openshift/application.yml -p APP_VERSION=${DEPLOYMENT_VERSION} | ${oc} apply -f -"
            sh "${oc} tag ${DEV_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION} ${TEST_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION}"
            sh "${oc} rollout latest dc/${APP_NAME}"
            sh "${oc} rollout status dc/${APP_NAME} --watch"
        }

    }

}

stage('Approve Promotion') {
    milestone 3
    input 'Proceed to PROD?'
    milestone 4
}

node('maven') {

    checkout scm

    dir(APP_NAME) {

        stage('Deploy to PROD') {
            def oc = "oc --namespace=${PROD_PROJECT}"
            sh "${oc} process -f openshift/application.yml -p APP_VERSION=${DEPLOYMENT_VERSION} | ${oc} apply -f -"
            sh "${oc} tag ${TEST_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION} ${PROD_PROJECT}/${APP_NAME}:${DEPLOYMENT_VERSION}"
            sh "${oc} rollout latest dc/${APP_NAME}"
            sh "${oc} rollout status dc/${APP_NAME} --watch"
        }

    }

    milestone 5

}

 */