# Sai Vandan CRM

<<<<<<< HEAD
Industry-ready real estate CRM and operational ERP for Sai Vandan Complex. The platform starts with secure lead management and is structured to grow into bookings, collections, inventory, vendors, payroll, support, and financial reporting.
## Included foundation
- Java 21 / Spring Boot REST API with PostgreSQL and Flyway migrations
- JWT authentication, refresh tokens, role/permission model, password hashing and audit fields
- Lead enquiry, assignment, qualification, follow-up and dashboard APIs
- Premium React + TypeScript interface using the Sai Vandan emerald, ivory and sandstone design system
- Docker configuration for PostgreSQL and a role-aware seed administrator
- Database foundation for projects, inventory, bookings, payments, vendors, HR, payroll, petty cash, support and approvals
=======
Sai Vandan CRM is a role-based real-estate CRM and operational ERP for Sai Vandan Complex. It includes sales, inventory, customer lifecycle, finance, HR/payroll, procurement, support, notifications, reporting, audit, and deployment assets.

## Technology

- Backend: Java 21, Spring Boot 3.5, Spring Security, JPA/JdbcTemplate
- Database: PostgreSQL 16 in production; H2 local profile for fast demos
- Migrations: Flyway, with parallel H2 and PostgreSQL tracks through V12
- Frontend: React, TypeScript, Vite
- Authentication: JWT access/refresh tokens, rotation, logout, sessions, throttling, RBAC
- API documentation: OpenAPI at `/api/v1/swagger-ui.html`
>>>>>>> 8f47717 (Implemented all phases)

## Roles

The application has exactly seven role workspaces: Super Admin, Sales Manager, Sales Executive, HR & Payroll, Accounts & Finance, Vendor Manager, and Customer Support. Each role receives its own sidebar, dashboard metrics, API authorization, data scope, and operational actions.

## Local development

### Backend

```powershell
Set-Location backend
& ..\.tools\apache-maven-3.9.10\bin\mvn.cmd -Dmaven.repo.local=..\.tools\m2 spring-boot:run
```

The local profile uses an in-memory H2 database and seeds realistic Sai Vandan records on startup. The API is available at `http://localhost:8080/api/v1`.

### Frontend

```powershell
Set-Location frontend
npm install
npm run dev
```

The Vite application is available at `http://localhost:5173/`.

### Demo credentials

Every demo account uses `ChangeMe!2026` locally:

| Role | Email |
|---|---|
| Super Admin | `admin@saivandan.local` |
| Sales Manager | `sales.manager@saivandan.local` |
| Sales Executive | `sales.executive@saivandan.local` |
| HR & Payroll | `hr@saivandan.local` |
| Accounts & Finance | `finance@saivandan.local` |
| Vendor Manager | `vendor@saivandan.local` |
| Customer Support | `support@saivandan.local` |

Change all demo passwords before any non-local deployment.

## Docker deployment

1. Copy `.env.example` to `.env` and replace every development secret.
2. Start the full stack:

```powershell
docker compose up --build -d
```

3. Open `http://localhost:5173/` and check the API health endpoint at `http://localhost:8080/api/v1/actuator/health`.
4. Stop the stack with `docker compose down`; add `-v` only when intentionally deleting the PostgreSQL volume.

The Compose stack contains PostgreSQL, the Spring Boot API, and an Nginx-served React build. The production profile automatically runs PostgreSQL Flyway migrations and validates the schema.

## Phase 9 reporting and notifications

- Persistent, role-scoped notification feed with unread/read-all actions, due-date escalation, and preferences.
- Report catalog and live data APIs for lead funnel, inventory, collections, vendor outstanding, payroll, support SLA, and project profit/loss.
- CSV, Excel-compatible `.xls`, and PDF downloads with export audit records.
- Saved report views scoped to the signed-in user.

## Quality checks

```powershell
# Backend compile and tests
Set-Location backend
& ..\.tools\apache-maven-3.9.10\bin\mvn.cmd -Dmaven.repo.local=..\.tools\m2 test

# Frontend type-check and production bundle
Set-Location ..\frontend
npm run build
```

The backend release tests cover demo login, notification read state, report authorization, and report export. The frontend build performs TypeScript validation and Vite production bundling.

For an environment without Maven's cached Surefire provider, run the dependency-light integration smoke checks against a running local stack:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\release-smoke.ps1
```

## Project documentation

- [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) — phase status and acceptance gates
- `backend/src/main/resources/db/migration/` — PostgreSQL migrations
- `backend/src/main/resources/db/migration/h2/` — local H2 migrations
- `backend/.env.example` and `.env.example` — environment templates
- `docker-compose.yml` — full deployment stack

## Production checklist

- Use a unique JWT secret of at least 32 random bytes.
- Set a strong PostgreSQL password and restrict database network access.
- Configure `FRONTEND_URLS` to trusted origins only.
- Replace all demo users/passwords and review role assignments.
- Enable HTTPS at the reverse proxy/load balancer.
- Back up PostgreSQL before migrations and test restore procedures.
- Review audit logs and report export audits regularly.
- Do not expose H2 or development credentials in production.
