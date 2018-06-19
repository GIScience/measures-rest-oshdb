pipeline {
  agent any
  stages {
    stage('Build and Test') {
      steps {
         script {
          author = sh(returnStdout: true, script: 'git show -s --pretty=%an')
          echo author
          commiti= sh(returnStdout: true, script: 'git log -1')
          echo commiti
          reponame=sh(returnStdout: true, script: 'basename `git remote get-url origin` .git').trim()
          echo reponame
          gittiid=sh(returnStdout: true, script: 'git describe --tags --long  --always').trim()
          echo gittiid
          echo env.BRANCH_NAME
          echo env.BUILD_NUMBER
        }

        script {
          server = Artifactory.server 'HeiGIT Repo'
          rtMaven = Artifactory.newMavenBuild()
          rtMaven.resolver server: server, releaseRepo: 'main', snapshotRepo: 'main'
          rtMaven.deployer server: server, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
          rtMaven.deployer.deployArtifacts = false
          env.MAVEN_HOME = '/usr/share/maven'
        }

        script {
          buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean compile source:jar install'
        }
      }
    }

    stage ('Deploy'){
      when {
        expression {
          return env.BRANCH_NAME ==~ /(^master$)/
        }
      }
      steps {
        script {
          rtMaven.deployer.deployArtifacts buildInfo
          server.publishBuildInfo buildInfo
        }
      }
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', message: "Deployment of oshdb-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}. Is Artifactory running?" , rawMessage: true
        }
      }
    }

  }
}
