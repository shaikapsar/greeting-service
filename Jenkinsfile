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

  environment {
    //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
    IMAGE = readMavenPom().getArtifactId()
    VERSION = readMavenPom().getVersion()
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

       //sh 'mvn -U clean package'
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
      }
    }
    //environment {
    //  ECR_CREDENTIALS = credentials('jenkins-automation')
    //}
    stage('Build and Publish Image') {
      when {
        branch 'main'  //only run these steps on the master branch
      }

      steps {
        script{
          docker.withRegistry('https://996251668898.dkr.ecr.us-east-1.amazonaws.com', 'ecr:us-east-1:jenkins-automation') {
              echo 'Run integration tests here...'
           // def customImage = docker.build("devsecops/${env.IMAGE}:${env.VERSION}-${env.BUILD_ID}")
           // customImage.push()
          //}
          }
        }
      }

     stage('Deploy') {
      when {
        branch 'main'  //only run these steps on the master branch
      }
      agent {
        docker {
              // we can use the same image and workspace as we did previously
            reuseNode true
            image 'amazon/aws-cli:2.9.10'
          }
        }
        steps {
           //sh 'mvn sonar:sonar -Dsonar.login=$SONAR_PSW'
          sh 's3 ls'
          }
        }
     }
  }

  


  //post {
  //  failure {
      // notify users when the Pipeline fails
      //mail to: 'iaijaz702@gmail.com',
      //    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
      //    body: "Something is wrong with ${env.BUILD_URL}"
  //  }
  //}
}