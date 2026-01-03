# Fintech App – CI/CD Pipeline (Jenkins + SonarQube + ECR + EKS)

This repository contains the full CI/CD automation pipeline for the Fintech App, a Spring Boot application for tracking credit card expenses.

Jenkins (SSH Build Node)

Maven Build

SonarQube Code Quality Scan

AWS ECR Image Build & Push

AWS EKS Kubernetes Deployment

Instance Profile AWS Authentication (No static creds!)

The pipeline is optimized for enterprise DevOps workflows and cloud-native deployments.

📌 High-Level Architecture
                 ┌────────────────────────┐
                 │   Developers Commit     │
                 │    (GitHub / GitLab)    │
                 └────────────┬───────────┘
                              │ Webhook / Poll
                              ▼
                   ┌────────────────────┐
                   │     Jenkins CI     │
                   │ (Controller Node)  │
                   └──────────┬─────────┘
                              │ SSH
                              ▼
              ┌─────────────────────────────────┐
              │      Build Node (EC2 Linux)     │
              │   - Maven Build                 │
              │   - SonarQube Scan              │
              │   - Docker Build & Push         │
              │   - kubectl Deploy to EKS       │
              │   - Uses Instance Profile Auth  │
              └───────────┬─────────────────────┘
                          │
                          ▼
            ┌──────────────────────────┐
            │       AWS ECR            │
            │  (Stores Docker Images)  │
            └──────────┬───────────────┘
                       │
                       ▼
            ┌──────────────────────────┐
            │         AWS EKS          │
            │ (Kubernetes Deployment)  │
            └──────────────────────────┘

🎯 Pipeline Capabilities

✔ Automated Maven build
✔ Automated SonarQube Quality Scan
✔ Docker image build and push to ECR
✔ Automated Kubernetes deployment using EKS
✔ Optional manual approval checkpoint
✔ Automatic account detection using EC2 Instance Profile
✔ Zero static AWS credentials required
✔ Supports multiple environments: dev, qa, uat, prod
✔ Kustomize overlays for environment-specific deployments

🧩 Prerequisites

Your Jenkins build node must have:

sudo apt install docker.io kubectl openjdk-17-jdk maven awscli -y


Your Jenkins controller must have:

Jenkins configured with:

SSH Agent Plugin

Pipeline Plugin

Credentials Plugin

SonarQube Plugin (optional)

SonarQube server must have:

A project named: fintech-app

A token stored in Jenkins (SONAR_TOKEN)

⚙️ 1. Configure the Jenkins Build Node (EC2)
1️⃣ Create the Linux user
sudo useradd -m sonar -s /bin/bash
sudo passwd sonar

2️⃣ Add the SSH key for Jenkins

On the build node:

sudo su - sonar
mkdir -p ~/.ssh
chmod 700 ~/.ssh
nano ~/.ssh/authorized_keys   # paste Jenkins private key's public part
chmod 600 ~/.ssh/authorized_keys

3️⃣ Give Docker permission
sudo usermod -aG docker sonar


Logout & login again.

4️⃣ Create Jenkins workspace directory
sudo mkdir -p /home/sonar/jenkins
sudo chown -R sonar:sonar /home/sonar/jenkins

🔐 2. Configure Jenkins Controller
1️⃣ Add SSH Credentials

In Manage Jenkins → Credentials → Global:

Kind: SSH Username with Private Key

ID: ssh-sonarqube

Username: sonar

Private Key: paste private key

2️⃣ Add SonarQube Credentials

Secret Text:

ID: SONAR_TOKEN

TEXT: <SonarQube token>

Secret Text:

ID: SONAR_HOST_URL

TEXT: http://<your-sonarqube-server>:9000

3️⃣ Add Node

Manage Jenkins → Nodes → New Node

Name: maven-sonarqube-build-node

Remote root: /home/sonar/jenkins

Labels: maven-sonarqube

Launch method: SSH

Credentials: ssh-sonarqube

Click Save → Jenkins will launch the node.

🛡 3. AWS Permissions via Instance Profile

Attach an IAM role with the following permissions to the EC2 build node:

Must-Have Policies:
ECR
ecr:GetAuthorizationToken
ecr:BatchGetImage
ecr:GetDownloadUrlForLayer
ecr:PutImage

EKS
eks:DescribeCluster

STS
sts:GetCallerIdentity

Kubernetes RBAC (inside EKS cluster)

Map the instance profile role in:

aws-auth ConfigMap

🧪 4. Pipeline Execution Flow
▶ When the pipeline runs:
1. Checkout

Pulls the source code.

2. Build

Runs Maven:

mvn clean package -DskipTests

3. SonarScan

Runs static analysis:

mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN

4. Image Build + Push

Builds and pushes Docker image to ECR.

5. Approval Gate (only on main/release)

Jenkins asks:

Deploy image <tag> to <environment>?

6. Deploy to EKS

Kustomize overlay patch + rollout:

kubectl apply -k ./k8s/overlays/<env>

🧭 5. Running the Pipeline Manually

In Jenkins:

Click "Build With Parameters"

Choose:

ENVIRONMENT = dev | qa | uat | prod

IMAGE_TAG optional

REGION = us-east-2

Click Build

🛠 6. Troubleshooting Guide
❗ Jenkins cannot SSH into the build node

Check:

sudo tail -f /var/log/auth.log


Verify SSH key permissions:

chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh

❗ Docker fails with permission denied
sudo usermod -aG docker sonar
newgrp docker

❗ ECR login fails

Ensure instance profile attached to EC2 has:

ecr:GetAuthorizationToken

❗ EKS authentication fails

Ensure the instance profile IAM role is mapped in:

kubectl edit configmap aws-auth -n kube-system


Add:

mapRoles:
- rolearn: arn:aws:iam::<ACCOUNT_ID>:role/<InstanceProfileRole>
  username: ci-user
  groups:
    - system:masters

🏁 7. Conclusion

This pipeline is engineered for:

High reliability

Multi-environment deployments

Full AWS cloud-native integration

Enterprise DevSecOps workflows

Secure authentication via Instance Profile

Zero credentials stored in Jenkins