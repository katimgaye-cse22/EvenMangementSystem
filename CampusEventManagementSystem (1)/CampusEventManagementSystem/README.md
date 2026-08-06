# Campus Event Management System

A layered Spring Boot web application for managing university events — students can browse, search, and register for events; administrators can create, update, and manage events and view participant lists.

## Status
🚧 **Build in progress** — this project is being built out phase by phase.

- [x] Phase 1 — Project setup (Maven, `application.properties`, main class)
- [x] Phase 2 — Database (schema, entities, relationships)
- [x] Phase 3 — Backend (controllers, services, repositories, DTOs, mappers, exception handling)
- [x] Phase 4 — Frontend (Thymeleaf pages, CSS, JS)
- [x] Phase 5 — Advanced features (optimistic locking, sorting, custom validation, popularity report, unit tests)
- [ ] Phase 4 — Frontend (Thymeleaf pages, CSS, JS)
- [ ] Phase 5 — Advanced features (JPQL/native queries, pagination, search, `@Transactional`, validation)
- [ ] Phase 6 — Final polish (ER diagram, API docs, report, slides)

## Tech Stack
- **Backend:** Java 21, Spring Boot 3.3, Spring MVC, Spring Data JPA, Hibernate, Spring Security
- **Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap, JavaScript
- **Database:** MySQL
- **Build tool:** Maven

## Getting Started

1. Install MySQL and make sure it's running locally.
2. Create the database (or let the app create it automatically — see `application.properties`):
   ```sql
   CREATE DATABASE campus_event_db;
   ```
3. Update `src/main/resources/application.properties` with your MySQL username/password.
4. Run the app:
   ```bash
   mvn spring-boot:run
   ```
5. Visit `http://localhost:8080`

### Test accounts
A seed admin account is created automatically on first run (see `data.sql`):
- **Email:** `admin@campus.edu`
- **Password:** `Admin@1234`

Create a student account yourself via the **Sign up** page.

## API Endpoints (Phase 3)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a student account |
| GET | `/api/auth/me` | Authenticated | Current logged-in user |
| POST | `/login` | Public | Login (Spring Security form login, JSON response) |
| POST | `/logout` | Authenticated | Logout |
| GET | `/api/events` | Public | List/search events (`keyword`, `category`, `page`, `size`) |
| GET | `/api/events/{id}` | Public | Event details |
| GET | `/api/events/categories` | Public | Distinct category list |
| POST | `/api/events` | ADMIN | Create event |
| PUT | `/api/events/{id}` | ADMIN | Update event |
| DELETE | `/api/events/{id}` | ADMIN | Delete event |
| POST | `/api/events/{eventId}/register` | STUDENT | Register for an event |
| DELETE | `/api/events/{eventId}/register` | STUDENT | Cancel registration |
| GET | `/api/registrations/my` | STUDENT | My registered events |
| GET | `/api/admin/events/{eventId}/participants` | ADMIN | Participant list |
| GET | `/api/admin/events/{eventId}/participants/export` | ADMIN | Participant list as CSV |
| GET | `/api/admin/reports/popular-events?limit=5` | ADMIN | Most-registered events (JPQL aggregate) |

`GET /api/events` also accepts `sortBy` (`eventDate`, `title`, `capacity`, `registeredCount` — whitelisted) and `sortDir` (`asc`/`desc`).

## Advanced features (Phase 5)

- **Optimistic locking** — `Event.version` (`@Version`) means two near-simultaneous writes to the same event (e.g. two students grabbing the last seat) can't silently clobber each other; the loser gets a clean 409 instead of a corrupted count.
- **Custom cross-field validation** — `@ValidEventTimes` on `EventDTO` enforces `endTime > startTime` at the Bean Validation layer, not buried in service code.
- **Aggregate JPQL report** — `RegistrationRepository.findMostPopularEvents` uses `GROUP BY` + `COUNT` to rank events by registrations, surfaced as an admin dashboard panel.
- **Sorting** — `/api/events` takes `sortBy`/`sortDir`, validated against a field whitelist.
- **Tests** — `RegistrationServiceTest`, `EventServiceTest`, `AuthServiceTest` (Mockito unit tests on the business logic) and `EventRepositoryTest` (`@DataJpaTest` against an embedded H2 database, so `mvn test` runs without MySQL). Run with:
  ```bash
  mvn test
  ```

## Project Structure
```
src/main/java/com/campus/eventmanagement/
├── config/          # Spring Security & Web configuration
├── controller/       # REST/MVC controllers
├── dto/               # Data Transfer Objects
├── entity/            # JPA entities
├── enums/             # Role, RegistrationStatus
├── exception/         # Custom exceptions + global handler
├── mapper/            # Entity <-> DTO mapping
├── repository/        # Spring Data JPA repositories
├── service/           # Business logic
└── util/              # Helpers (Constants, DateUtil)
```
