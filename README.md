# Risk Register

A full-stack risk-register take-home project for recording risks, assessing their inherent and residual exposure, and tracking mitigations.

The application uses a Java/Spring Boot API and a React/TypeScript client. Its Git history uses small `test:` then `feat:` commits to make the test-driven development process visible.

## What it does

- Creates, views, updates, filters, and deletes risks.
- Records risk category, owner, likelihood, impact, and lifecycle status.
- Calculates inherent and residual scores and their severity bands.
- Creates, views, updates, and deletes mitigations per risk.
- Prevents closing a risk until it has at least one mitigation.
- Validates all 1–5 ratings and returns clear API error messages.
- Sorts the API risk dashboard by residual score, highest first.

## Architecture

```text
React + TypeScript (Vite, :5173)
             |
             | /api proxy in development
             v
Spring Boot REST API (:8080)
             |
             v
Service / domain rules / scoring
             |
             v
Spring Data JPA -> H2 in-memory database
```

The backend follows `controller -> service -> repository -> entity` boundaries. Controllers translate HTTP requests; services apply business rules; repositories persist data; entities protect domain state and relationships.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22 LTS and npm

## Run locally

Start the backend in one PowerShell window:

```powershell
cd C:\Users\shaki\Documents\Projects\risk-register\backend
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" spring-boot:run
```

Start the frontend in another:

```powershell
cd C:\Users\shaki\Documents\Projects\risk-register\frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). Keep both processes running: Vite sends `/api` calls to Spring Boot on port 8080.

H2 is in-memory, so application data resets when the backend stops.

## Verify the project

Backend tests:

```powershell
cd C:\Users\shaki\Documents\Projects\risk-register\backend
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" test
```

Frontend tests and production build:

```powershell
cd C:\Users\shaki\Documents\Projects\risk-register\frontend
npm test
npm run build
```

## User workflow

1. Select **Add risk** and enter its title, description, category, owner, likelihood, impact, and status.
2. Use the category/status selectors to focus the dashboard.
3. Select a risk to inspect its score details and mitigation list.
4. Add a mitigation with an effectiveness rating. Residual score updates from the API response.
5. Edit/delete either resource as needed. Closing a risk without mitigation is rejected with a readable explanation.

## Risk model and scoring

Each risk contains:

| Field | Values / meaning |
| --- | --- |
| Category | Operational, Financial, Compliance, Security, Strategic |
| Status | Open, Mitigating, Closed |
| Likelihood and impact | Integer ratings from 1 to 5 |
| Mitigation | Description and effectiveness rating from 1 to 5 |

```text
inherent score = likelihood × impact

remaining fraction for one mitigation = (6 - effectiveness) / 6

residual score = max(1, ceil(inherent × product(remaining fractions)))
```

For example, a risk with likelihood 4 and impact 5 has an inherent score of 20. One mitigation with effectiveness 5 leaves `1/6` of that score: `ceil(20 × 1/6) = 4`.

| Score | Severity |
| --- | --- |
| 1–5 | Low |
| 6–12 | Medium |
| 13–19 | High |
| 20–25 | Critical |

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/risks` | Create a risk |
| `GET` | `/api/risks?category=SECURITY&status=OPEN` | List/filter risks; residual score descending |
| `GET` | `/api/risks/{id}` | Get one risk |
| `PUT` | `/api/risks/{id}` | Update a risk |
| `DELETE` | `/api/risks/{id}` | Delete a risk |
| `POST` | `/api/risks/{id}/mitigations` | Add mitigation |
| `GET` | `/api/risks/{id}/mitigations` | List mitigations for a risk |
| `PUT` | `/api/risks/{riskId}/mitigations/{mitigationId}` | Update mitigation |
| `DELETE` | `/api/risks/{riskId}/mitigations/{mitigationId}` | Delete mitigation |

## Design decisions and alternatives

| Decision | Choice made | Why | Alternative and trade-off |
| --- | --- | --- | --- |
| Local database | H2 in-memory, in PostgreSQL compatibility mode | One-command setup and fast isolated tests; no database installation or Docker required for an evaluator. | PostgreSQL from day one provides realistic behaviour and durable data, but adds setup complexity. |
| Persistence abstraction | JPA/Spring Data repositories | Keeps the service layer independent of SQL and makes a later database switch small. | JDBC/jOOQ gives more explicit SQL and tuning control, at the cost of more persistence code. |
| Production schema evolution | Hibernate `create-drop` for the assignment | Automatically creates a clean local schema. | Flyway + versioned SQL migrations is the production choice; it preserves and evolves real data. |
| Backend structure | Controller, service, repository, entity layers | Makes HTTP concerns, business rules, and persistence independently testable. | Putting logic in controllers is quicker initially but becomes hard to reuse and test. |
| Mitigation API shape | Nested under `/api/risks/{riskId}/mitigations` | The parent risk is explicit and accidental cross-risk updates are avoided. | Top-level `/api/mitigations` is useful for organisation-wide mitigation reporting, but requires ownership validation on every request. |
| Risk score | Likelihood × impact, range 1–25 | Familiar risk-matrix calculation that is transparent to users and reviewers. | Weighted or additive models can express business priorities, but require agreed weighting policy. |
| Mitigation calculation | Compound remaining-risk fractions | Multiple mitigations reduce the remaining exposure without allowing a simple fixed subtraction to overstate control impact. | Fixed score subtraction is easier to explain but treats mitigation effect unrealistically. |
| Minimum residual | Score is never lower than 1 | A mitigation controls risk; it does not prove all risk disappeared. | Allowing 0 could represent fully eliminated risk if the organisation explicitly defines it that way. |
| Closure rule | At least one mitigation required before `Closed` | Encodes the stated governance decision in one central domain rule. | A warning-only workflow is more flexible but cannot enforce the control. |
| Validation | Jakarta Bean Validation at the request boundary plus domain checks | Invalid input gets a clear 4xx response before persistence; domain rules still hold if code is reused elsewhere. | Database-only constraints protect data but produce less friendly API errors. |
| Frontend tooling | React + TypeScript + Vite | Small, fast developer experience with type-safe API models and no unnecessary server framework. | Next.js is valuable when SSR, routing, or server components are needed; those add complexity here. |
| Frontend/backend connection | Vite `/api` proxy in development | Avoids CORS setup locally and lets client code use stable relative URLs. | Configure CORS and use absolute API URLs; necessary for separate production deployments. |
| Testing approach | Tests specified before feature behaviour in focused commits | Demonstrates TDD intent and keeps calculation and workflow rules safe during changes. | End-to-end-only testing gives stronger browser coverage but is slower and less precise for domain rules. |

## Assignment-focused trade-offs

This project intentionally does not add authentication/authorisation, pagination, audit history, file attachments, background jobs, Docker, or production deployment configuration. Those would be natural next steps for a production risk platform, but would distract from the assignment's core risk, mitigation, scoring, and testing requirements.

The UI uses native browser confirmation for destructive actions to keep the client dependency-free. A production version would normally use a reusable accessible confirmation dialog and provide optimistic updates/retries where appropriate.

## TDD evidence

The commit history contains paired `test:` and `feat:` commits for scoring rules, severity bands, risk closure rules, persistence, REST endpoints, mitigation endpoints, and the client risk-creation workflow. Tests cover unit-level scoring and domain behaviour, JPA persistence, Spring MVC API integration, and React component behaviour.
