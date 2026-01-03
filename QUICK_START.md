# Quick Start Guide - Fintech App

This guide provides instructions for getting the Fintech App project running locally and setting up the CI/CD pipeline.

## 🚀 Local Development

### 1. Prerequisites
- Java 17
- Maven 3.9+
- Docker (optional, for MySQL)

### 2. Run Locally
```bash
# Clone the repo
git clone <repo-url>
cd fintech-app-jenkins

# Run MySQL (optional, if you have docker-compose)
docker-compose up -d

# Build and run the app
mvn clean package
java -jar target/fintech-app-1.0.0.jar
```
The app will be available at `http://localhost:8080`.

---

## 🏗 CI/CD Setup (Jenkins)

### 1. Configure Jenkins Controller
1. **SSH Credentials**: Add `ssh-sonarqube` (Username: `sonar`, Private Key).
2. **SonarQube Credentials**: Add `SONAR_TOKEN` (Secret Text) and `SONAR_HOST_URL`.
3. **Add Node**: Create `maven-sonarqube-build-node` with label `maven-sonarqube`.

### 2. Configure Build Node (EC2)
1. **Install Dependencies**:
   ```bash
   sudo apt update
   sudo apt install docker.io kubectl openjdk-17-jdk maven awscli -y
   ```
2. **Setup IAM Role**: Attach an IAM role with ECR, EKS, and STS permissions.
3. **Map Role in EKS**: Update the `aws-auth` ConfigMap in your EKS cluster.

### 3. Run the Pipeline
1. In Jenkins, select "Build with Parameters".
2. Choose the `ENVIRONMENT` (dev, qa, uat, prod).
3. Click "Build".

---

## ☸️ Kubernetes Deployment

The project uses **Kustomize** for deployments.

- **Base Manifests**: `k8s/base/`
- **Environment Overlays**: `k8s/overlays/`

To apply manually:
```bash
kubectl apply -k k8s/overlays/dev
```
