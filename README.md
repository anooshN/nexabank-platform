# NexaBank — Production-Grade Full Stack Banking Application

> A complete enterprise banking platform built with Java 17, Spring Boot 3.2, Spring AI, Angular 17, React 18, Kafka, RabbitMQ, PostgreSQL, MongoDB, Redis — ready to deploy with Docker Compose or Kubernetes.

## Features

### Core Banking
- User registration & login with JWT + OAuth2
- Bank account management (Savings, Checking, Fixed Deposit)
- Money transfers with optimistic locking (no double-spend)
- Deposit & withdrawal
- Real-time transaction history

### AI-Powered (Spring AI + GPT-4o)
- **Fraud detection** — every transaction scored in <50ms
- **NexaBot chatbot** — natural language banking assistant
- **Spending insights** — AI-generated weekly financial analysis
- **pgvector** for transaction embedding similarity

### Messaging
- **Kafka** — 20,000+ TPS, 12 partitions, exactly-once semantics
- **RabbitMQ** — email notification queues with dead-letter retry
- Real-time transaction events, audit events, fraud alerts

### Email Notifications
- Transaction confirmation emails (HTML templates)
- Welcome emails on account creation
- Fraud alert emails
- Password reset emails

### Audit
- Every event persisted to MongoDB
- CSV export endpoint for compliance
- Fraud score tracking per transaction
- Full event replay capability

### Security
- Spring Security + JWT Bearer tokens
- BCrypt password hashing (strength 12)
- Role-based access (ROLE_CUSTOMER, ROLE_ADMIN)
- Rate limiting per route via Redis
- JWT filter at API Gateway level

### DevOps
- Multi-stage Dockerfiles (JRE-only runtime images)
- Docker Compose — one command local startup
- Kubernetes manifests with HPA (3→20 pods for transaction-service)
- GitHub Actions CI/CD (test → build → push → deploy)
- Prometheus metrics + Grafana dashboards

## Quick Start

```bash
git clone <your-repo>
cd nexabank
cp .env.example .env
# Edit .env with your OPENAI_API_KEY and MAIL credentials
docker-compose up -d
```

Open http://localhost:4200

See [DEPLOYMENT.md](./DEPLOYMENT.md) for full instructions.

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| Language | Java 17, TypeScript, JavaScript |
| Backend | Spring Boot 3.2, Spring Cloud Gateway, Spring Security, Spring AI |
| ORM | Hibernate 6, Spring Data JPA |
| Messaging | Apache Kafka, RabbitMQ |
| Databases | PostgreSQL, MongoDB, Redis |
| AI | Spring AI 1.x, OpenAI GPT-4o, pgvector |
| Frontend | Angular 17 + Material, React 18 + Vite |
| DevOps | Docker, Kubernetes, GitHub Actions, Helm |
| Monitoring | Prometheus, Grafana, Spring Actuator |
| Security | JWT, OAuth2, BCrypt, Rate Limiting |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Login, get tokens |
| POST | /api/auth/refresh | Refresh access token |
| POST | /api/accounts | Open bank account |
| GET | /api/accounts/user/{username} | Get accounts |
| POST | /api/transactions/transfer | Money transfer |
| POST | /api/transactions/deposit | Deposit |
| GET | /api/transactions/account/{acctNum} | History |
| POST | /api/ai/chat | Chat with NexaBot |
| POST | /api/ai/fraud/check | Fraud analysis |
| GET | /api/audit | All audit logs (admin) |
| GET | /api/audit/export | Download CSV |

Full Swagger UI: http://localhost:8081/swagger-ui.html

## Project Structure

```
nexabank/
├── api-gateway/              Spring Cloud Gateway + JWT filter
├── auth-service/             Registration, login, JWT, BCrypt
├── account-service/          Account CRUD, Redis cache, Kafka
├── transaction-service/      Transfers, Kafka, fraud check
├── notification-service/     RabbitMQ + JavaMailSender
├── audit-service/            Kafka consumer, MongoDB, CSV export
├── ai-service/               Spring AI fraud + chatbot (GPT-4o)
├── frontend-angular/         Customer portal (Angular 17)
├── frontend-react/           Admin dashboard (React 18)
├── docker-compose.yml        Local full stack
├── k8s/                      Kubernetes production manifests
├── monitoring/               Prometheus + Grafana config
├── .github/workflows/        GitHub Actions CI/CD
└── DEPLOYMENT.md             Step-by-step deployment guide
```
