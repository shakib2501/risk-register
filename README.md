# Risk Register

A full-stack risk-register take-home project. The backend is implemented with Java 21, Spring Boot, Spring Data JPA, and H2. The React/TypeScript frontend will live in `frontend/` once Node.js is available on the development machine.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22 LTS and npm (required when setting up the frontend)

## Run the backend

From PowerShell:

```powershell
cd C:\Users\shaki\Documents\Projects\risk-register\backend
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" spring-boot:run
```

The API starts at `http://localhost:8080`.

## Run the frontend

In a second PowerShell window:

```powershell
cd C:\Users\shaki\Documents\Projects\risk-register\frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` calls to the Spring Boot server, so keep both processes running. The frontend has its own checks:

```powershell
npm test
npm run build
```

## User workflow

- Add, edit, filter, and delete risks from the dashboard.
- Select a risk to view its inherent and residual scores, severity bands, and mitigation count.
- Add, edit, or delete mitigations from the risk detail panel.
- A risk may be closed only after at least one mitigation exists; API validation messages are shown in the interface.

H2 is an in-memory database, so data resets when the backend stops. This is intentional for the assignment's simple local setup.

Run the tests with:

```powershell
mvn "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" test
```

The explicit Maven repository option keeps Maven dependencies in the current Windows user profile.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/risks` | Create a risk |
| `GET` | `/api/risks?category=SECURITY&status=OPEN` | List/filter risks, ordered by residual score descending |
| `GET` | `/api/risks/{id}` | Get one risk |
| `PUT` | `/api/risks/{id}` | Update a risk |
| `DELETE` | `/api/risks/{id}` | Delete a risk |
| `POST` | `/api/risks/{id}/mitigations` | Add a mitigation |
| `GET` | `/api/risks/{id}/mitigations` | List mitigations |
| `PUT` | `/api/risks/{riskId}/mitigations/{mitigationId}` | Update a mitigation |
| `DELETE` | `/api/risks/{riskId}/mitigations/{mitigationId}` | Delete a mitigation |

Risk categories: `OPERATIONAL`, `FINANCIAL`, `COMPLIANCE`, `SECURITY`, `STRATEGIC`.

Risk statuses: `OPEN`, `MITIGATING`, `CLOSED`.

## Risk scoring

Inherent score is `likelihood × impact`, where both values must be integers from 1 to 5.

Each mitigation leaves a fraction of the risk behind: `(6 - effectiveness) / 6`. Mitigations compound, and the residual score is rounded up and constrained to at least 1:

```text
residual = max(1, ceil(inherent × product((6 - effectiveness) / 6)))
```

This gives higher-effectiveness mitigations more influence without allowing any mitigation to erase risk completely. With no mitigations, residual score equals inherent score.

Severity bands are: Low (1–5), Medium (6–12), High (13–19), Critical (20–25).

A risk cannot transition to `CLOSED` until it has at least one mitigation.

## Database choice

H2 runs in memory and requires no separate installation, which keeps assignment setup simple. The datasource uses H2's PostgreSQL compatibility mode and the application uses JPA, so moving to PostgreSQL later is mainly a configuration change. A production deployment would use PostgreSQL with migrations (for example Flyway), rather than H2.

## Test-driven workflow

The Git history deliberately alternates `test:` and `feat:` commits for the main behavior slices. The tests cover score calculation and bands, persistence, API validation, risk CRUD, mitigation CRUD, and the mitigation-before-close workflow.
