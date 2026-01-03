#!/bin/bash
#
# === CONFIGURATION ===
AWS_ACCOUNT_ID="999568710647"
AWS_REGION="us-east-2"
ECR_REPO_NAME="fintech-app"
EKS_NAMESPACE="fintech"
DOMAIN_NAME="dominionsystem.org"
APP_PORT=8080

# === GO TO PROJECT ROOT ===
cd "$(dirname "$0")" || exit

# === ENSURE DIRECTORIES EXIST ===
echo "📁 Checking folders..."
for dir in k8s/base k8s/overlays/dev k8s/overlays/qa k8s/overlays/uat k8s/overlays/prod eks_addons/script; do
  if [ ! -d "$dir" ]; then
    echo "Creating $dir..."
    mkdir -p "$dir"
  fi
done

echo "🚀 Checking Dockerfile..."
if [ ! -f Dockerfile ]; then
  cat <<EOF > Dockerfile
# -------- STAGE 1: Build the application --------
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# -------- STAGE 2: Create minimal runtime image --------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
  echo "Dockerfile created."
else
  echo "Dockerfile already exists, skipping."
fi

echo "🌐 Checking Kubernetes manifests..."

# Base Deployment
if [ ! -f k8s/base/deployment.yaml ]; then
  cat <<EOF > k8s/base/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
  namespace: $EKS_NAMESPACE
spec:
  replicas: 3
  selector:
    matchLabels:
      app: fintech-app
  template:
    metadata:
      labels:
        app: fintech-app
    spec:
      containers:
      - name: fintech-app
        image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:latest
        ports:
        - containerPort: $APP_PORT
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:mysql://mysql-service.$EKS_NAMESPACE.svc.cluster.local:3306/fintech
        - name: SPRING_DATASOURCE_USERNAME
          value: root
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: root-password
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: $APP_PORT
          initialDelaySeconds: 60
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: $APP_PORT
        resources:
          requests:
            cpu: "500m"
            memory: "512Mi"
          limits:
            cpu: "1"
            memory: "1Gi"
EOF
  echo "k8s/base/deployment.yaml created."
fi

# Dev Overlay
if [ ! -f k8s/overlays/dev/patch-deployment.yaml ]; then
  cat <<EOF > k8s/overlays/dev/patch-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: fintech-app
        image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:IMAGE_TAG
EOF
  echo "k8s/overlays/dev/patch-deployment.yaml created."
fi

if [ ! -f k8s/overlays/dev/kustomization.yaml ]; then
  cat <<EOF > k8s/overlays/dev/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- ../../base
patchesStrategicMerge:
- patch-deployment.yaml
namePrefix: dev-
EOF
  echo "k8s/overlays/dev/kustomization.yaml created."
fi

echo "✅ Project setup check completed!"
echo ""
echo "🔑 Next Steps:"
echo "1. Review the generated/existing files in k8s/ and Dockerfile"
echo "2. Ensure your Jenkins pipeline is configured with the correct credentials"
echo "3. Push your changes to the repository to trigger the pipeline 🚀"

