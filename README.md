# FXDealManager

A secure, maintainable bank money exchange deal processor built with Spring Boot, JPA, Docker, and PostgreSQL.

## Features

- Import money exchange deals from Excel files (`.xlsx`)
- Validates for missing/invalid data and skips duplicates before saving
- Robust error handling and professional logging to a file (`deal-import.log`)
- Saves valid deals to a PostgreSQL database via Docker
- Convenient REST API endpoint for Excel uploads
- Unit tests cover service and repository logic

---

## Quick Start

### Prerequisites

- Java 17+
- Maven
- Docker Desktop (no need to install PostgreSQL manually)
- Postman (or curl) for API testing

---

### Clone and Build
git clone https://github.com/Abdo-Radi/fx-deal-manager.git
cd fx-deal-manager
mvn clean install

---

### Run PostgreSQL with Docker
docker compose up -d
Launches a PostgreSQL server on port 5432, ready for the app.

---

### Configure Application

Edit `src/main/resources/application.properties` if needed:

spring.datasource.url=jdbc:postgresql://localhost:5432/fxdealdb
spring.datasource.username=fxuser
spring.datasource.password=fxpassword
logging.file.name=deal-import.log


---

### Start the Application

./mvnw clean spring-boot:run

Or run `ProgresssoftApplication.java` in your IDE.

---

### Import Deals

- Use **Postman**:
    - POST request to `http://localhost:8080/api/deals/import`
    - Body: Select "form-data", key: `file`, type: `File`, add your `test-file.xlsx`
    - Click **Send**
- The response will be a log listing what happened to each deal row (saved, skipped, error).

---

### API Response Example
```json

[
  "Row 2: Deal [DEAL-001] saved.",
  "Row 3 Skipped: Duplicate Unique ID [DEAL-001]",
  "Row 4 Error: Invalid timestamp format: 2024-01-15"
] \```

---

## Testing & Validation

- Automated unit tests live in `src/test/java`
- Manual test: upload Excel with valid and invalid rows—API and logs should match expected results
- Database table `deals` can be browsed in pgAdmin or DBeaver; only valid deals are saved

---

## Troubleshooting

- **Docker not running:** Start Docker Desktop.
- **400 errors:** Check the file format, key name (`file`), and ensure file size is <10MB.
- **Database not connecting:** Ensure `docker compose up -d` succeeds and correct credentials are used.

---
## Project Structure
├── src/
│ ├── main/java/com/progresssoft/fxdealmanager/
│ │ ├── controller/DealController.java
│ │ ├── model/Deal.java
│ │ ├── repository/DealRepository.java
│ │ ├── service/DealService.java
│ │ └── exception/GlobalExceptionHandler.java
│ └── resources/application.properties
├── docker-compose.yml
├── pom.xml
└── README.md
---

## How It Works

- Reads each deal from Excel, validates fields, skips bad/duplicate rows
- Saves valid deals instantly—no rollback on later errors
- Error handling returns friendly messages; all actions and errors are logged

---





## Author

- Abdellah Radi, 2025

---

## Contributions

Feel free to fork, submit issues, or open pull requests!

