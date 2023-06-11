pipeline {

    agent any

    environment {
        WAIT_HOSTS = "localhost:5432, localhost:9444"
        WAIT_HOSTS_TIMEOUT = 300
        WAIT_SLEEP_INTERVAL = 30
        WAIT_CONNECT_TIMEOUT = 30
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Preparo do ambiente') {
            steps {
                script {
                    dir('src/docker') {
                        sh "docker-compose down && docker-compose build && docker-compose up -d"
                    }
                }
            }
            post {
                failure {
                    zulipSend stream: 'itexto', topic: 'Itexto - Boot itexto', message: "**@all** Erro no build #${BUILD_NUMBER} do projeto ${JOB_NAME} - Preparo do ambiente"
                }
            }
        }

        stage('Testes automatizados') {
            tools {
                jdk "jdk16"
                maven "3.8.1"
            }
            steps {
                script {
                    dir('src/itexto-boot') {
                        sh "chmod +x ./wait"
                        sh "./wait && mvn clean package"
                    }
                }
            }
            post {
                always {
                    junit "src/itexto-boot/target/**/*.xml"
                    script {
                        dir('src/docker') {
                            sh "docker-compose down"
                        }
                    }
                }
                failure {
                    zulipSend stream: 'itexto', topic: 'Itexto - Boot itexto', message: "**@all** Erro no build #${BUILD_NUMBER} do projeto ${JOB_NAME} - Testes automatizados"
                }
            }
        }
        stage('Validação da imagem Docker') {
            steps {
                script {
                    dir('src/itexto-boot') {
                        sh "docker build -t boot-itexto:latest ."
                    }
                }
            }
            post {
                success {
                    script {
                        sh "docker rmi boot-itexto:latest"
                    }
                }
                failure {
                    zulipSend stream: 'itexto', topic: 'Itexto - Boot itexto', message: "**@all** Erro no build #${BUILD_NUMBER} do projeto ${JOB_NAME} - Validação de imagem"
                }
            }
        }
    }
}
