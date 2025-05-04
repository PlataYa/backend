# PlataYa - Electronic Wallet API

## Project Overview
PlataYa is an e-wallet application similar to PayPal or MercadoPago, developed as part of the Software Quality Assurance course (1st Quarter 2025). The project consists of a backend API that serves both a mobile app and a web application.

## Features
- Virtual account (wallet) management
- User registration with email and password
- Balance inquiry
- Transaction history
- P2P money transfers between users
- External payment methods simulation (load/withdraw funds)

## Tech Stack
- **Backend**: Kotlin + Spring Boot
- **Database**: PostgreSQL
- **Build Tool**: Gradle
- **Containerization**: Docker

## Prerequisites
- Java 21+
- Docker & Docker Compose
- PostgreSQL (or use the provided Docker container)
- Gradle 8.13

## Running the Application

### Using Docker
1. Start the postgres database:
```bash
docker-compose up -d
```

2. To stop the services:
```bash
docker-compose down
```

### Using Gradle
1. Set up environment variables for database connection
2. Run the application:
```bash
./gradlew bootRun
```

## Testing
The project includes:
- Unit tests with JUnit
- Integration tests for repository and service layers
- Stress tests using Locust

To run tests:
```bash
./gradlew test
```

## Project Structure
```
/
├── src/
│   ├── main/
│   │   ├── kotlin/plataya/app/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   └── test/
├── docker-compose.yml
├── build.gradle.kts
└── README.md
```

## CI/CD
The project uses GitHub Actions for continuous integration, including:
- Code compilation
- Unit and integration testing
- Docker image building and publishing

## Versioning
This project follows Semantic Versioning (SemVer)