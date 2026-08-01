# Sai Vandan CRM

Industry-ready real estate CRM and operational ERP for Sai Vandan Complex. The platform starts with secure lead management and is structured to grow into bookings, collections, inventory, vendors, payroll, support, and financial reporting.
## Included foundation
- Java 21 / Spring Boot REST API with PostgreSQL and Flyway migrations
- JWT authentication, refresh tokens, role/permission model, password hashing and audit fields
- Lead enquiry, assignment, qualification, follow-up and dashboard APIs
- Premium React + TypeScript interface using the Sai Vandan emerald, ivory and sandstone design system
- Docker configuration for PostgreSQL and a role-aware seed administrator
- Database foundation for projects, inventory, bookings, payments, vendors, HR, payroll, petty cash, support and approvals

## Local setup

1. Start PostgreSQL: `docker compose up -d`
2. Copy `backend/.env.example` to `backend/.env` if required and set secure secrets.
3. From `backend`, run `mvn spring-boot:run`.
4. From `frontend`, run `npm install` then `npm run dev`.

The backend defaults to `http://localhost:8080/api/v1`; the frontend starts on `http://localhost:5173`.

## Local demo accounts

On first start, the application creates exactly seven role workspaces. Every local demo account uses the password `ChangeMe!2026`:

- Super Admin: `admin@saivandan.local`
- Sales Manager: `sales.manager@saivandan.local`
- Sales Executive: `sales.executive@saivandan.local`
- HR & Payroll: `hr@saivandan.local`
- Accounts & Finance: `finance@saivandan.local`
- Vendor Manager: `vendor@saivandan.local`
- Customer Support: `support@saivandan.local`

Change this immediately outside local development.

## Development roadmap

1. Completed: platform foundation, authentication, lead pipeline, dashboards, role model.
2. Next: inventory, site visits, quotations, approvals and booking flows.
3. Then: payments, agreements, loan processing, possession and customer portal.
4. Then: vendors, petty cash, HR/payroll, support, audit reports and integrations.
