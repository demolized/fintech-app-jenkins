# Fintech App - Codebase Explanation & Implementation Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Technology Stack](#technology-stack)
5. [Core Components](#core-components)
6. [CI/CD Pipeline](#cicd-pipeline)
7. [Kubernetes Deployment](#kubernetes-deployment)
8. [How to Implement New Features](#how-to-implement-new-features)
9. [Development Workflow](#development-workflow)

---

## 🎯 Project Overview

**Fintech App** is a **Credit Card Expense Tracker** application built with:
- **Backend**: Java 17 + Spring Boot 3.1.4
- **Database**: MySQL (via JPA/Hibernate)
- **Frontend**: Thymeleaf templates + HTML/CSS
- **CI/CD**: Jenkins with SonarQube integration
- **Containerization**: Docker
- **Orchestration**: Kubernetes (AWS EKS)
- **Infrastructure**: AWS (ECR for container registry, EKS for orchestration)

### Key Features:
✅ Track credit card expenses across multiple cards  
✅ Categorize spending (Food, Transport, Entertainment, etc.)  
✅ Dashboard with visual charts (category breakdown, vendor totals, daily spend)  
✅ Export expenses to CSV  
✅ Health checks via Spring Boot Actuator  
✅ Multi-environment deployment (dev, qa, uat, prod)

---

## 🏗 Architecture

### High-Level Flow
```
Developer → GitHub → Jenkins → Maven Build → SonarQube Scan → Docker Build → ECR → EKS Deployment
```

### Component Architecture
```
┌─────────────────────────────────────────────────┐
│           Fintech Spring Boot App              │
│  ┌──────────────┐    ┌──────────────┐         │
│  │ Controllers  │───▶│   Services   │         │
│  └──────┬───────┘    └──────┬───────┘         │
│         │                   │                  │
│         ▼                   ▼                  │
│  ┌──────────────┐    ┌──────────────┐         │
│  │  Thymeleaf   │    │ Repositories │         │
│  │  Templates   │    └──────┬───────┘         │
│  └──────────────┘           │                  │
│                              ▼                  │
│                       ┌─────────────┐          │
│                       │   MySQL DB  │          │
│                       └─────────────┘          │
└─────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
fintech-app-jenkins/
├── src/
│   └── main/
│       ├── java/com/fintech/app/
│       │   ├── FintechApplication.java          # Main entry point
│       │   ├── config/
│       │   │   └── DataSeeder.java               # Sample data initialization
│       │   ├── controller/
│       │   │   ├── ExpenseController.java        # Expense routes
│       │   │   └── CreditCardController.java     # Card management routes
│       │   ├── entity/
│       │   │   ├── Expense.java                  # Expense entity/model
│       │   │   └── CreditCard.java               # Credit card entity/model
│       │   ├── repository/
│       │   │   ├── ExpenseRepository.java        # Data access for expenses
│       │   │   └── CreditCardRepository.java     # Data access for cards
│       │   └── service/
│       │       ├── ExpenseService.java           # Business logic for expenses
│       │       ├── ChartService.java             # Chart data aggregation
│       │       └── ChartServiceImpl.java         # Chart implementation
│       └── resources/
│           ├── application.yml                    # App configuration
│           ├── templates/                         # Thymeleaf HTML templates
│           │   ├── index.html                    # Landing page
│           │   ├── expenses.html                 # Expense list/add form
│           │   ├── dashboard.html                # Analytics dashboard
│           │   └── credit-card.html              # Card management
│           └── static/
│               ├── css/                           # Stylesheets
│               └── assets/                        # Images/icons
├── k8s/
│   ├── base/                                      # Base Kubernetes manifests
│   │   ├── deployment.yaml                       # App deployment
│   │   ├── service.yaml                          # App service
│   │   ├── mysql.yaml                            # MySQL deployment
│   │   ├── mysql-service.yaml                    # MySQL service
│   │   ├── mysql-pvc.yaml                        # Persistent volume claim
│   │   ├── secret-mysql.yaml                     # DB credentials
│   │   ├── ingress.yaml                          # Ingress rules
│   │   ├── hpa.yaml                              # Horizontal Pod Autoscaler
│   │   └── storageclass.yaml                     # Storage configuration
│   └── overlays/                                  # Environment-specific configs
│       ├── dev/
│       │   ├── kustomization.yaml                # Dev kustomization
│       │   └── patch-deployment.yaml             # Dev-specific patches
│       ├── qa/
│       ├── uat/
│       └── prod/
├── eks_addons/
│   ├── monitoring/                                # Prometheus/Grafana configs
│   ├── elk/                                       # ELK stack configs
│   └── script/                                    # Helm installation scripts
├── pom.xml                                        # Maven build configuration
├── Dockerfile                                     # Multi-stage Docker build
├── docker-compose.yaml                            # Local development setup
├── jenkinsfile                                    # Jenkins pipeline definition
└── README.md                                      # Project documentation
```

---

## 🛠 Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 17 | Modern Java with LTS support |
| **Framework** | Spring Boot 3.1.4 | Web framework + dependency injection |
| **Web** | Spring MVC | REST controllers and routing |
| **View Engine** | Thymeleaf | Server-side HTML templating |
| **ORM** | Spring Data JPA + Hibernate | Database abstraction |
| **Database** | MySQL 8.0 | Relational data storage |
| **Validation** | Jakarta Validation (JSR-380) | Input validation |
| **Build Tool** | Maven 3.9.4 | Dependency management & build |
| **Containerization** | Docker | Application packaging |
| **Orchestration** | Kubernetes (EKS) | Container orchestration |
| **CI/CD** | Jenkins | Automation pipeline |
| **Code Quality** | SonarQube | Static code analysis |
| **Monitoring** | Spring Boot Actuator | Health checks & metrics |
| **Cloud** | AWS (ECR, EKS, EC2) | Hosting & container registry |

---

## 🔧 Core Components

### 1. **Entities (Database Models)**

#### `CreditCard.java`
```java
@Entity
public class CreditCard {
    @Id @GeneratedValue
    private Long id;
    private String cardHolderName;
    private String maskedNumber;      // e.g., "**** 1234"
    private String cardType;           // Visa, MasterCard, Amex
    private LocalDate expiry;
    private double creditLimit;        // Spending cap
}
```

#### `Expense.java`
```java
@Entity
public class Expense {
    @Id @GeneratedValue
    private Long id;
    private String vendor;             // e.g., "Starbucks"
    private double amount;
    private LocalDate date;
    private String category;           // Food, Transport, etc.
    
    @ManyToOne
    private CreditCard card;           // Which card was used
}
```

**Relationship**: One credit card can have many expenses (`@ManyToOne`)

---

### 2. **Repositories (Data Access Layer)**

Using Spring Data JPA interfaces:

```java
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Inherited: findAll(), findById(), save(), delete(), etc.
    // Custom queries defined in implementation
}

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    // CRUD operations auto-generated
}
```

---

### 3. **Services (Business Logic)**

#### `ExpenseService.java`
- `findMonthlyExpenses()` - Get current month's expenses
- `findExpensesByCardIdThisMonth(Long cardId)` - Filter by card
- `calculateTotalForCardThisMonth(Long cardId)` - Sum expenses
- `save(Expense expense)` - Add new expense

#### `ChartService.java` / `ChartServiceImpl.java`
- `getCategoryTotals(Long cardId)` - Aggregate spending by category
- `getVendorTotals(Long cardId)` - Top vendors for a card
- `getDailySpend(Long cardId)` - Daily spending trend

---

### 4. **Controllers (Route Handlers)**

#### `ExpenseController.java`

| Route | Method | Description |
|-------|--------|-------------|
| `/` | GET | Landing page |
| `/expenses` | GET | List all expenses + add form |
| `/expenses` | POST | Add new expense |
| `/expenses/dashboard` | GET | Analytics dashboard with charts |
| `/expenses/export` | GET | Download CSV of expenses |

**Example Flow:**
1. User visits `/expenses`
2. Controller calls `expenseService.findMonthlyExpenses()`
3. Data passed to Thymeleaf template `expenses.html`
4. Page renders with expense table

---

### 5. **Frontend (Thymeleaf Templates)**

Located in `src/main/resources/templates/`:

- **`index.html`** - Home page
- **`expenses.html`** - Expense list + form to add expense
- **`dashboard.html`** - Charts using Chart.js (category pie chart, vendor bar chart, daily line chart)
- **`credit-card.html`** - Manage credit cards

**Chart.js Integration**: Dashboard uses Chart.js to render visual analytics based on data from `ChartService`.

---

## 🚀 CI/CD Pipeline

### Jenkins Pipeline (`jenkinsfile`)

The pipeline runs on a **remote SSH build node** with the label `maven-sonarqube`.

#### Pipeline Stages:

```
1. Checkout
   └─ Pull source code from GitHub

2. Tooling Setup
   └─ Verify Java, Maven, Docker, kubectl, AWS CLI

3. Build with Maven
   └─ Run: mvn clean package -DskipTests
   └─ Archive JAR artifact

4. SonarQube Scan
   └─ Run code quality analysis
   └─ Only on non-PR branches

5. AWS Identity Check
   └─ Verify instance profile authentication

6. Resolve AWS Account ID
   └─ Extract account ID for ECR URL

7. Determine Image Tag
   └─ Use provided tag or generate timestamp (yyyyMMddHHmmss)

8. Login to ECR
   └─ aws ecr get-login-password | docker login

9. Build & Push Docker Image
   └─ docker build -t <ECR_URL>:<TAG> .
   └─ docker push <ECR_URL>:<TAG>

10. Deploy Gate (Manual Approval)
    └─ Only on 'main' or 'release' branches
    └─ Wait for user to approve deployment

11. Deploy to EKS
    └─ Update kubeconfig for the selected environment
    └─ Robustly patch deployment YAML with new image tag using sed
    └─ Apply Kustomize overlay for the selected environment
```

### Key Features:
- ✅ **No static AWS credentials** (uses EC2 instance profile)
- ✅ **Automatic account detection**
- ✅ **Environment-based deployment** (dev/qa/uat/prod)
- ✅ **Manual approval gate** for production safety
- ✅ **Robust image patching** logic for reliability
- ✅ **Automatic Docker cleanup** after build

---

## ☸️ Kubernetes Deployment

### Kustomize Structure

The app uses **Kustomize** for environment-specific configurations:

```
k8s/
├── base/                          # Common configs for all environments
│   ├── deployment.yaml            # 3 replicas, readiness/liveness probes
│   ├── service.yaml               # ClusterIP service on port 8080
│   ├── mysql.yaml                 # MySQL StatefulSet
│   └── ...
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml     # namePrefix: dev-
    │   └── patch-deployment.yaml  # 2 replicas, dev namespace
    ├── qa/                         # namePrefix: qa-
    ├── uat/                        # namePrefix: uat-
    └── prod/                       # namePrefix: prod-
```

### Deployment Configuration

**Base Deployment** (`k8s/base/deployment.yaml`):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
  namespace: fintech
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: fintech-app
        image: <ECR_URL>:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:mysql://prod-mysql-service.fintech.svc.cluster.local:3306/fintech
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
            port: 8080
          initialDelaySeconds: 60
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
        resources:
          requests:
            cpu: 500m
            memory: 512Mi
          limits:
            cpu: 1
            memory: 1Gi
```

**Overlay Patch** (e.g., `k8s/overlays/dev/patch-deployment.yaml`):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
  namespace: fintech-dev  # Dev-specific namespace
spec:
  replicas: 2             # Fewer replicas for dev
  template:
    spec:
      containers:
      - name: fintech-app
        image: <ACCOUNT_ID>.dkr.ecr.us-east-2.amazonaws.com/fintech-app:IMAGE_TAG
```

The Jenkins pipeline **dynamically replaces** `IMAGE_TAG` with the actual built image tag.

---

## 💡 How to Implement New Features

### Scenario 1: Add a New Field to Expenses

**Goal**: Track "payment method" (Cash, Card, UPI) for each expense.

#### Step 1: Update Entity
**File**: `src/main/java/com/fintech/app/entity/Expense.java`

```java
@Entity
public class Expense {
    // ... existing fields ...
    
    private String paymentMethod;  // NEW FIELD
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
```

#### Step 2: Update Database
Since `ddl-auto: update` is enabled in `application.yml`, Hibernate will **auto-create** the column on next run.

#### Step 3: Update Frontend Form
**File**: `src/main/resources/templates/expenses.html`

```html
<form th:action="@{/expenses}" th:object="${expense}" method="post">
    <!-- ... existing fields ... -->
    
    <label>Payment Method:</label>
    <select th:field="*{paymentMethod}">
        <option value="Card">Card</option>
        <option value="Cash">Cash</option>
        <option value="UPI">UPI</option>
    </select>
    
    <button type="submit">Add Expense</button>
</form>
```

#### Step 4: Update Display Table
**File**: `src/main/resources/templates/expenses.html`

```html
<table>
    <thead>
        <tr>
            <th>Vendor</th>
            <th>Amount</th>
            <th>Payment Method</th> <!-- NEW COLUMN -->
        </tr>
    </thead>
    <tbody>
        <tr th:each="expense : ${expenses}">
            <td th:text="${expense.vendor}"></td>
            <td th:text="${expense.amount}"></td>
            <td th:text="${expense.paymentMethod}"></td> <!-- NEW -->
        </tr>
    </tbody>
</table>
```

#### Step 5: Test Locally
```bash
mvn spring-boot:run
```
Navigate to `http://localhost:8080/expenses` and verify the new field.

#### Step 6: Commit & Push
```bash
git add .
git commit -m "Add payment method field to expenses"
git push origin main
```

Jenkins will automatically:
1. Build the app
2. Run SonarQube scan
3. Build Docker image
4. Push to ECR
5. Deploy to selected environment

---

### Scenario 2: Add a New REST API Endpoint

**Goal**: Create an API to get total spending for a specific category.

#### Step 1: Add Repository Query
**File**: `src/main/java/com/fintech/app/repository/ExpenseRepository.java`

```java
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category")
    Double getTotalByCategory(@Param("category") String category);
}
```

#### Step 2: Add Service Method
**File**: `src/main/java/com/fintech/app/service/ExpenseService.java`

```java
@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;
    
    public Double getCategoryTotal(String category) {
        return expenseRepository.getTotalByCategory(category);
    }
}
```

#### Step 3: Create REST Controller
**File**: `src/main/java/com/fintech/app/controller/ApiController.java` (NEW FILE)

```java
package com.fintech.app.controller;

import com.fintech.app.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    private final ExpenseService expenseService;
    
    public ApiController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    @GetMapping("/category-total")
    public Double getCategoryTotal(@RequestParam String category) {
        return expenseService.getCategoryTotal(category);
    }
}
```

#### Step 4: Test the API
```bash
curl "http://localhost:8080/api/category-total?category=Food"
```

Expected output:
```json
1250.50
```

---

### Scenario 3: Add Environment-Specific Configuration

**Goal**: Use different MySQL hosts for dev vs prod.

#### Step 1: Update Base Deployment
**File**: `k8s/base/deployment.yaml`

Keep the production MySQL URL as default.

#### Step 2: Create Dev Overlay Patch
**File**: `k8s/overlays/dev/patch-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
spec:
  template:
    spec:
      containers:
      - name: fintech-app
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:mysql://dev-mysql-service.fintech-dev.svc.cluster.local:3306/fintech_dev
```

#### Step 3: Apply with Kustomize
```bash
kubectl apply -k ./k8s/overlays/dev
```

Kustomize will **merge** the base + dev patch, resulting in dev-specific database URL.

---

## 🔄 Development Workflow

### Local Development

1. **Clone Repository**
   ```bash
   git clone <repo-url>
   cd fintech-app-jenkins
   ```

2. **Run MySQL via Docker Compose**
   ```bash
   docker-compose up -d
   ```
   This starts MySQL on port 3306 with credentials from `docker-compose.yaml`.

3. **Run Application**
   ```bash
   mvn spring-boot:run
   ```
   App starts on `http://localhost:8080`

4. **Make Changes**
   Edit Java files, templates, or CSS.

5. **Test Locally**
   Access the app in browser and verify changes.

6. **Build JAR**
   ```bash
   mvn clean package
   ```
   JAR created in `target/fintech-app-1.0.0.jar`

---

### CI/CD Deployment Workflow

1. **Developer commits code**
   ```bash
   git add .
   git commit -m "Feature: Add payment method tracking"
   git push origin main
   ```

2. **Jenkins detects change** (webhook or polling)

3. **Pipeline executes:**
   - Maven build
   - SonarQube scan
   - Docker build
   - ECR push

4. **Manual approval** (for main/release branches)
   Jenkins pauses and asks: "Deploy to DEV?"

5. **Deployment to EKS**
   - Updates kubeconfig
   - Patches deployment YAML with new image
   - Applies Kustomize overlay
   - Kubernetes performs rolling update

6. **Verification**
   ```bash
   kubectl get pods -n fintech-dev
   kubectl logs -n fintech-dev <pod-name>
   ```

---

## 📊 Key Endpoints

| URL | Description |
|-----|-------------|
| `http://localhost:8080/` | Landing page |
| `http://localhost:8080/expenses` | Expense list & add form |
| `http://localhost:8080/expenses/dashboard` | Analytics dashboard |
| `http://localhost:8080/expenses/export` | Download CSV |
| `http://localhost:8080/actuator/health` | Health check endpoint |
| `http://localhost:8080/actuator/info` | App info |

---

## 🔒 Security & Best Practices

1. **No Hardcoded Credentials**
   - Database password stored in Kubernetes Secret
   - AWS authentication via EC2 instance profile

2. **Health Checks**
   - Readiness probe ensures pod only receives traffic when ready
   - Liveness probe restarts unhealthy pods

3. **Resource Limits**
   - CPU/memory limits prevent resource exhaustion
   - Horizontal Pod Autoscaler (HPA) for auto-scaling

4. **Code Quality**
   - SonarQube scans every build
   - Enforces coding standards

5. **Immutable Infrastructure**
   - Every deployment creates new Docker image
   - Tagged with timestamp for rollback capability

---

## 🎓 Summary

This is a **production-ready Spring Boot fintech application** with:

- ✅ **3-tier architecture** (Controller → Service → Repository)
- ✅ **Fully automated CI/CD** pipeline with Jenkins
- ✅ **Multi-environment Kubernetes deployment** using Kustomize
- ✅ **Cloud-native design** with health checks, resource limits, and auto-scaling
- ✅ **Code quality enforcement** via SonarQube
- ✅ **Secure credential management** via Kubernetes Secrets

To implement new features:
1. **Update entity/model** if database changes needed
2. **Add business logic** in Service layer
3. **Create/modify controller** for new routes
4. **Update frontend templates** for UI changes
5. **Commit & push** - Jenkins handles the rest!

---

## 📚 Additional Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Thymeleaf Docs**: https://www.thymeleaf.org/
- **Kustomize Docs**: https://kustomize.io/
- **Jenkins Pipeline**: https://www.jenkins.io/doc/book/pipeline/
- **Kubernetes Docs**: https://kubernetes.io/docs/

Happy coding! 🚀
