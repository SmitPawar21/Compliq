pipeline {
    agent any

    environment {
        // Replace with your AWS account ID and region if using ECR
        // AWS_ACCOUNT_ID = '123456789012'
        // AWS_REGION = 'us-east-1'
        // ECR_REPO_BACKEND = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/compliq-backend"
        // ECR_REPO_FRONTEND = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/compliq-frontend"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test & Build Images') {
            steps {
                // The Dockerfiles have been configured to run tests (or linting) during the build process.
                // We use docker compose to build both backend and frontend images locally.
                sh 'docker compose build backend frontend'
            }
        }

        // stage('Push to AWS ECR') {
        //     steps {
        //         // Example of how to push to ECR (requires AWS CLI installed and IAM permissions)
        //         // sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        //         // sh "docker tag compliq_backend:latest ${ECR_REPO_BACKEND}:latest"
        //         // sh "docker push ${ECR_REPO_BACKEND}:latest"
        //         // sh "docker tag compliq_frontend:latest ${ECR_REPO_FRONTEND}:latest"
        //         // sh "docker push ${ECR_REPO_FRONTEND}:latest"
        //     }
        // }

        stage('Deploy (Docker Compose)') {
            steps {
                // Deploy the newly built containers.
                // --no-deps ensures only backend and frontend are recreated, leaving mysql and jenkins running.
                sh 'docker compose up -d --no-deps backend frontend nginx'
            }
        }
    }
    
    post {
        always {
            // Clean up dangling images to save space on the free tier EC2 instance
            sh 'docker image prune -f'
        }
    }
}
