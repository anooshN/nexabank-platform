# NexaBank — Complete Deployment Guide

## What You Have

```
nexabank/
├── api-gateway/           # Spring Cloud Gateway (port 8080)
├── auth-service/          # JWT + OAuth2 Auth (port 8081)
├── account-service/       # Bank Accounts (port 8082)
├── transaction-service/   # Money Transfers + Kafka (port 8083)
├── notification-service/  # Email + RabbitMQ (port 8084)
├── audit-service/         # MongoDB Audit Logs (port 8085)
├── ai-service/            # Spring AI Fraud + Chatbot (port 8086)
├── frontend-angular/      # Customer Portal (port 4200)
├── frontend-react/        # Admin Dashboard (port 3000)
├── docker-compose.yml     # Full local stack
├── k8s/                   # Kubernetes manifests
├── monitoring/            # Prometheus + Grafana
└── .github/workflows/     # GitHub Actions CI/CD
```

---

## OPTION 1 — Local Development (Docker Compose)

### Prerequisites
- Docker Desktop 4.x+
- Docker Compose v2+
- 8 GB RAM minimum (16 GB recommended)
- OpenAI API key (for AI features)

### Step 1 — Clone and configure

```bash
cd nexabank
cp .env.example .env
```

Edit `.env`:
```env
OPENAI_API_KEY=sk-your-key-here
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-gmail-app-password
JWT_SECRET=nexabank-super-secret-key-must-be-at-least-32-chars-long
```

### Step 2 — Start the entire stack

```bash
docker-compose up -d
```

Wait ~3 minutes for all services to start. Check status:
```bash
docker-compose ps
docker-compose logs -f api-gateway
```

### Step 3 — Verify services

| Service               | URL                              | Purpose              |
|-----------------------|----------------------------------|----------------------|
| Customer Portal       | http://localhost:4200            | Angular frontend     |
| Admin Dashboard       | http://localhost:3000            | React admin panel    |
| API Gateway           | http://localhost:8080            | Main entry point     |
| Swagger UI            | http://localhost:8081/swagger-ui.html | API docs         |
| Kafka UI              | http://localhost:9090            | Kafka topics         |
| RabbitMQ Management   | http://localhost:15672           | Queue management     |
| Prometheus            | http://localhost:9091            | Metrics              |
| Grafana               | http://localhost:3001            | Dashboards           |

### Step 4 — Test the APIs

```bash
# Register a customer
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"Pass@1234","role":"ROLE_CUSTOMER"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"Pass@1234"}'
# Copy the accessToken from response

# Create an account (replace TOKEN)
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","fullName":"John Doe","email":"john@example.com","accountType":"SAVINGS","initialDeposit":5000,"currency":"USD"}'

# Make a transfer
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fromAccountNumber":"NXB...","toAccountNumber":"NXB...","amount":100,"description":"Test transfer"}'

# Ask the AI chatbot
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"What is my account balance?","conversationId":"session-1"}'
```

### Stop the stack
```bash
docker-compose down           # Stop but keep data
docker-compose down -v        # Stop and delete all data
```

---

## OPTION 2 — Build from Source (Maven)

### Prerequisites
- Java 17 (JDK)
- Maven 3.9+
- Node.js 20+
- PostgreSQL, MongoDB, Redis, Kafka, RabbitMQ running locally

### Step 1 — Build all Java services

```bash
cd nexabank
mvn clean package -DskipTests
```

### Step 2 — Run each service

Open separate terminals:
```bash
# Terminal 1
cd auth-service && java -jar target/*.jar

# Terminal 2
cd account-service && java -jar target/*.jar

# Terminal 3
cd transaction-service && java -jar target/*.jar

# Terminal 4
cd notification-service && java -jar target/*.jar

# Terminal 5
cd audit-service && java -jar target/*.jar

# Terminal 6
cd ai-service && java -jar target/*.jar

# Terminal 7 (last — needs all others running)
cd api-gateway && java -jar target/*.jar
```

### Step 3 — Run Angular frontend

```bash
cd frontend-angular
npm install
npm start
# Opens at http://localhost:4200
```

### Step 4 — Run React admin

```bash
cd frontend-react
npm install
npm run dev
# Opens at http://localhost:3000
```

---

## OPTION 3 — Production Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (EKS / GKE / AKS / bare metal)
- kubectl configured
- Helm 3.x
- GitHub Container Registry access

### Step 1 — Push images via GitHub Actions

Push to `main` branch — the CI/CD pipeline automatically:
1. Runs all tests
2. Builds Docker images
3. Pushes to GitHub Container Registry
4. Deploys to Kubernetes

### Step 2 — Manual Kubernetes deploy

```bash
# Create namespace
kubectl apply -f k8s/nexabank-k8s.yml

# Set your secrets (IMPORTANT — change all values!)
kubectl create secret generic nexabank-secrets \
  --from-literal=JWT_SECRET="your-256-bit-secret" \
  --from-literal=DB_PASSWORD="your-db-password" \
  --from-literal=OPENAI_API_KEY="sk-your-key" \
  --from-literal=REDIS_PASSWORD="your-redis-pass" \
  -n nexabank

# Deploy
kubectl apply -f k8s/nexabank-k8s.yml

# Check pods
kubectl get pods -n nexabank

# Get gateway external IP
kubectl get svc api-gateway -n nexabank
```

### Step 3 — Set up databases in Kubernetes (using Helm)

```bash
# PostgreSQL
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres-auth bitnami/postgresql \
  --set auth.username=nexabank \
  --set auth.password=nexabank123 \
  --set auth.database=nexabank_auth \
  -n nexabank

# MongoDB
helm install mongodb bitnami/mongodb \
  --set auth.username=nexabank \
  --set auth.password=nexabank123 \
  --set auth.database=nexabank_audit \
  -n nexabank

# Redis
helm install redis bitnami/redis \
  --set auth.password=nexabank_redis \
  -n nexabank

# Kafka
helm repo add strimzi https://strimzi.io/charts/
helm install kafka strimzi/strimzi-kafka-operator -n nexabank
```

### Step 4 — Scale for 20,000+ TPS

```bash
# Scale transaction service
kubectl scale deployment transaction-service --replicas=10 -n nexabank

# HPA automatically scales 3-20 pods based on CPU/memory
kubectl get hpa -n nexabank
```

---

## Configure Gmail for Email Notifications

1. Go to Google Account → Security → 2-Step Verification → Enable
2. Search "App passwords" → Create one for "Mail"
3. Set in `.env`:
   ```
   MAIL_USERNAME=your@gmail.com
   MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx  (the 16-char app password)
   ```

---

## Configure OpenAI for AI Features

1. Get API key from https://platform.openai.com/api-keys
2. Set in `.env`:
   ```
   OPENAI_API_KEY=sk-proj-...
   ```
3. The AI service uses **GPT-4o** by default. Change in `ai-service/src/main/resources/application.yml`:
   ```yaml
   spring:
     ai:
       openai:
         chat:
           options:
             model: gpt-4o-mini  # cheaper alternative
   ```

---

## Demo Accounts (auto-seeded on first start)

| Username | Password    | Role           |
|----------|-------------|----------------|
| admin    | Admin@1234  | ROLE_ADMIN     |
| user1    | User@1234   | ROLE_CUSTOMER  |

---

## Monitoring

- **Grafana**: http://localhost:3001 → user: `admin` / `nexabank123`
- **Prometheus**: http://localhost:9091
- Each service exposes metrics at `/actuator/prometheus`

---

## Troubleshooting

```bash
# Service won't start
docker-compose logs <service-name>

# Kafka issues — restart Kafka
docker-compose restart kafka zookeeper

# Database connection issues
docker-compose restart postgres-auth

# Clear everything and restart fresh
docker-compose down -v && docker-compose up -d

# Check JWT secret is identical across all services
docker-compose exec auth-service env | grep JWT_SECRET
docker-compose exec api-gateway env | grep JWT_SECRET
```

---

## Architecture Summary

```
Browser → Nginx → Angular / React
                         ↓
                   API Gateway :8080
                     (JWT filter)
                    ↙  ↓   ↓  ↘
           Auth  Acct  Txn  AI  Audit  Notify
           8081  8082  8083 8086 8085  8084
                    ↓    ↓      ↓
                  Kafka Topics  RabbitMQ
                 (12 partitions)
               ↙   ↓      ↓     ↘
           PostgreSQL  MongoDB  Redis
           (3 DBs)   (Audit)  (Cache)
```

**Built with:** Java 17 · Spring Boot 3.2 · Spring AI · Spring Cloud Gateway ·
Apache Kafka · RabbitMQ · PostgreSQL · MongoDB · Redis · Angular 17 ·
React 18 · Docker · Kubernetes · GitHub Actions · Prometheus · Grafana
