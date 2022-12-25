import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

pipeline {

  /*
   * Run everything on an existing agent configured with a label 'docker'.
   * This agent will need docker, git and a jdk installed at a minimum.
   */
  agent {
    node {
      label 'docker'
    }
  }

  // using the Timestamper plugin we can add timestamps to the console log
  options {
    timestamps()
    skipStagesAfterUnstable()
  }

  parameters {
    credentials credentialType: 'com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl', defaultValue: 'ecr:us-east-1:jenkins-automation', description: 'ECR Credentails', name: 'ecr_credentails', required: false
    string defaultValue: 'us-east-1', description: 'AWS REGION ', name: 'REGION', trim: true
    string defaultValue: 'default', description: 'ECS CLUSTER', name: 'ECS_CLUSTER', trim: true
    string defaultValue: 'backend', description: 'ECS FAMILY', name: 'ECS_FAMILY', trim: true
    string defaultValue: 'subnet-0e8e34eeb5a9bf7cd', description: 'ECS_SUBNET', name: 'ECS_SUBNET', trim: true
    string defaultValue: 'sg-0cc9d656c78a0419e', description: 'ECS_SECURITY_GROUP', name: 'ECS_SECURITY_GROUP', trim: true
  } 

  environment {
    //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
    IMAGE = readMavenPom().getArtifactId()
    VERSION = readMavenPom().getVersion()
    ECR_CREDENTAILS = "${params.ecr_credentails}"
  }

  stages {

    stage('Build') {
      agent {
        docker {
          /*
           * Reuse the workspace on the agent defined at top-level of Pipeline but run inside a container.
           * In this case we are running a container with maven so we don't have to install specific versions
           * of maven directly on the agent
           */
          reuseNode true
          image 'maven:3.5.0-jdk-8'
        }
      }
      steps {
        // using the Pipeline Maven plugin we can set maven configuration settings, publish test results, and annotate the Jenkins console
        withMaven(options: [junitPublisher(ignoreAttachments: false)]) {
          //sh 'mvn clean findbugs:findbugs package'
          echo 'Run build here...'
        }
      }

      post {
        success {
          // we only worry about archiving the jar file if the build steps are successful
          //archiveArtifacts(artifacts: '**/target/*.jar', allowEmptyArchive: true)
          echo 'JUNIT here...'
        }
      }
    }

    stage('Quality Analysis') {
      parallel {
        // run Sonar Scan and Integration tests in parallel. This syntax requires Declarative Pipeline 1.2 or higher
        stage ('Integration Test') {
          agent any  //run this stage on any available agent
          steps {
            echo 'Run integration tests here...'
          }
        }
        stage('Sonar Scan') {
          agent {
            docker {
                // we can use the same image and workspace as we did previously
              reuseNode true
              image 'maven:3.5.0-jdk-8'
            }
          }
          // environment {
              //use 'sonar' credentials scoped only to this stage
          //   SONAR = credentials('sonar')
            //}
          steps {
            //sh 'mvn sonar:sonar -Dsonar.login=$SONAR_PSW'
            sh 'echo sonar'
          }
        }

      } //end of parallel
    } //end of Quality Analysis stage

    stage('SetEnvironment'){
      steps {
        script {
          // This step reloads the env with configured values for account number and region in various values.
          readProperties(file: 'aws.env').each { key, value -> env[key.toUpperCase()] = value }
        }
      }
    }

    stage('Build and Publish Image') {
      when {
        branch 'main'  //only run these steps on the master branch
      }

      steps {
        script{
          docker.withRegistry("${env.ECR_REGISTRY}", "${env.ECR_CREDENTAILS}") {
            echo 'Run integration tests here...'
            def customImage = docker.build("${env.ECR_REPO}/${env.IMAGE}:${env.VERSION}-${env.BUILD_ID}")
            customImage.push()
          //}
          }
        }
      }
    }  //end of Build and Publish Image

    stage('Deploy') {
      when {
        branch 'main'  //only run these steps on the master branch
      }
 /*     agent {
        docker {
           // we can use the same image and workspace as we did previously
          reuseNode true
          image 'amazon/aws-cli:2.9.10'
        }
      }*/

      steps {
        withAWS(region:'us-east-1', credentials: 'jenkins-automation') {

          // create vpc
          //sh encoding: 'UTF-8', label: 'VPC_CREATE', returnStatus: true, returnStdout: true, script: 'aws ec2 create-vpc --cidr-block "172.31.0.0/16"'

          // create ecs cluster
          sh encoding: 'UTF-8', script: "aws ecs create-cluster --cluster-name  ${params.ECS_CLUSTER}"

         // --network-mode


          sh encoding: 'UTF-8', label: 'CREATE-SERVICE', returnStatus: true, returnStdout: true, script: "aws ecs create-service --cluster ${params.ECS_CLUSTER} --service-name ${env.IMAGE} --task-definition test:2 --desired-count 1  --network-configuration \"awsvpcConfiguration={subnets=[${params.ECS_SUBNET}],securityGroups=[${params.ECS_SECURITY_GROUP}],assignPublicIp=ENABLED}\""

          //--launch-type \"FARGATE\"


        }
        
      }
    }  //end of Deploy

  } //end of stages

} //end of pipeline