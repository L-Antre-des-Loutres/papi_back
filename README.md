# PAPI

## What is PAPI?
**P**okémon **A**pplication **P**rogramming **I**nterface, or **Pokémon API**, is a RESTful API that provides access to a comprehensive database of Pokémon data. It allows developers to retrieve information about Pokémon, including their stats, abilities, types, and more.

It is designed for small fangame developers and enthusiasts who want to build their own Pokémon-related projects without having to set up their own database or API from scratch, or deal with larger projects that can be difficult to work with and adapt due to their size and complexity.

## Tech stack used

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.3 |
| Security | Spring Security + JWT (JJWT) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL (production), H2 (tests) |
| Mapping | MapStruct |
| Build | Maven |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker + Docker Compose |

## How to setup for development

1. Clone the repository
   ```bash
   git clone <repo-url>
   cd papi_back
   ```

2. Copy the environment template and fill in your values
   ```bash
   cp .env.example .env
   ```
   To generate a JWT secret:
   ```bash
   mvn exec:java -Dexec.mainClass=org.antredesloutres.papi.GenerateSecret
   ```

3. Start a local MySQL instance, or update `DB_URL` in `.env` to point to your database

4. Run the development server
   ```bash
   mvn spring-boot:run
   ```
   The API will be available at `http://localhost:8080`.
   Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## Useful commands

| Command | Description |
|---|---|
| `mvn spring-boot:run` | Start the app in dev mode |
| `mvn test` | Run the test suite |
| `mvn clean package -DskipTests` | Build the JAR |
