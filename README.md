# Restaurant order service

Spring Boot 4.1.1, Java 17+, Maven, JPA, H2, Lombok, and ModelMapper. OpenFeign uses Spring Cloud 2025.1.4.

## Run locally

Open this folder as a Maven project in IntelliJ. Let Maven download dependencies, then run OrderApplication.
Or, from this directory in PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Address: http://localhost:8082.
Start menu-service on 8081 before placing orders. MENU_SERVICE_URL overrides its address.
Each service owns a separate file-based H2 database under data/ (ignored by Git). No database installation is needed for this first stage. Run from this directory. H2 and automatic schema updates are local learning defaults; MySQL and migrations can follow later.

## Packages

- entity: database models.
- repository: JPA database access.
- dto: API request/response objects, including input validation.
- service: business interfaces.
- serviceimpl: business logic and ModelMapper conversions.
- controller: HTTP endpoints.
- config: shared Spring beans.
- exception: HTTP error handling using ProblemDetail.
- client: OpenFeign HTTP client for menu-service.

## Endpoints

- GET /api/orders
- GET /api/orders/{id}
- POST /api/orders
- PATCH /api/orders/{id}/status

Create example:
```json
{"customerName":"Alex","tableNumber":2,"items":[{"dishId":1,"quantity":2}]}
```
Status example: {"status":"PREPARING"}.
Transitions: PLACED -> PREPARING or CANCELLED; PREPARING -> SERVED or CANCELLED. Terminal orders cannot change status.
Dish names and prices are saved as snapshots. Totals are calculated in the backend. Each order has 1-50 lines; quantities are 1-100. Network failures prevent the order from being saved.

## Tests

```powershell
.\mvnw.cmd test
```

This first stage is for local learning. Authentication, Docker, GitHub Actions, Helm, and Kubernetes come later. Browser requests go through the Angular development proxy.

## Verification in the creation session

Backend builds/tests could not run because this sandbox could not download the missing ModelMapper and Spring Cloud dependencies from Maven Central. Import the Maven project in IntelliJ and run the tests after dependencies resolve. No running-backend or end-to-end success is claimed.

References: [Spring Cloud compatibility](https://spring.io/projects/spring-cloud/), [ModelMapper](https://modelmapper.org/getting-started/).
