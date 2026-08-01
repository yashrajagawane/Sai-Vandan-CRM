# Sai Vandan CRM - Completion Plan

## Objective

Deliver a production-ready Real Estate CRM/ERP for Sai Vandan Complex with seven role-specific workspaces, secure APIs, real domain workflows, reports, approvals, auditability, and a polished enterprise UI.

The existing login, role navigation, lead foundation, demo data, notifications, CSV/print reports, and sandstone visual system are preserved. Each phase below replaces scaffolded module behavior with real domain-backed functionality.

## Delivery rules

- Keep the seven-role model exactly as defined: Super Admin, Sales Manager, Sales Executive, HR & Payroll, Accounts & Finance, Vendor Manager, Customer Support.
- Enforce authorization in three places: frontend route/menu guards, Spring Security method guards, and service/query-level ownership filters.
- Never store raw Aadhaar, PAN, bank account, or salary data in logs or unmasked API responses.
- Every write workflow must record actor, timestamps, status, remarks, and audit history.
- Each phase must include migration updates, seed/demo data, API validation, UI wiring, and tests before it is marked complete.
- No generic `workspace_records` implementation may be used as the final source of truth for a business module.

## Phase 0 - Baseline and project controls

### Deliverables

- Freeze the current working baseline and document all existing endpoints, roles, routes, migrations, and demo credentials.
- Standardize API base URL and environment configuration for local, test, and production profiles.
- Make PostgreSQL and H2 migrations structurally equivalent for development and CI.
- Add an API error envelope, request correlation ID, structured logging, and health/readiness endpoints.
- Add OpenAPI/Swagger dependency and publish the initial role-permission matrix.

### Acceptance criteria

- Clean backend and frontend builds from a fresh checkout.
- Login works for all seven demo users.
- CI can start the database, run migrations, seed data, and execute tests.

## Phase 1 - Identity, RBAC, audit, and administration

### Deliverables

- Complete user management: create, edit, disable, reset password, role assignment, and session/device list.
- Add permission tables and permission checks beyond broad role checks.
- Add persistent audit events for every create, update, delete, approval, login, export, and sensitive read.
- Add soft delete and restore for administrator-controlled records.
- Add login throttling, refresh-token rotation/revocation, password-reset flow, and session logout.

### Acceptance criteria

- Super Admin can manage users and permissions.
- Other roles cannot access user administration or platform settings.
- Audit log shows before/after values and actor for protected changes.
- Security tests prove cross-role access is rejected.

## Phase 2 - Projects, inventory, pricing, and availability

### Deliverables

- Normalize Project -> Wing/Tower -> Floor -> Unit hierarchy.
- Add carpet area, built-up area, facing, parking, amenities, price history, status history, reservation expiry, and availability states.
- Add inventory grid, floor/tower filters, unit detail, reserve/release actions, and conflict checks.
- Add configurable price lists and project master data.

### Acceptance criteria

- Inventory Manager/Super Admin can create and update inventory.
- Sales users can only view permitted inventory.
- A unit cannot be reserved or booked twice.
- Every status and price change is audited.

## Phase 3 - Complete sales lifecycle

### Deliverables

- Lead source configuration, duplicate detection by mobile/email/name, merge history, lead scoring, and round-robin assignment.
- Lead qualification form and hot/warm/cold scoring.
- Follow-up calendar, reminders, overdue escalation, call outcome, attachments, and timeline.
- Site-visit booking, reschedule/no-show flow, feedback, pickup, photos, and conversion reporting.
- Negotiation and quotation versioning with price breakup, expiry, discount matrix, and manager approval.
- Booking workflow with payment validation, co-applicants, confirmation PDF, cancellation, refund, and inventory release approval.

### Acceptance criteria

- Sales Manager can assign/reassign and approve within thresholds.
- Sales Executive can operate only assigned leads.
- Approved quotations cannot be edited; revisions create a new version.
- Booking conflict prevention and approval tests pass.

## Phase 4 - Customer documents, loans, agreements, and possession

### Deliverables

- Customer and co-applicant master linked to leads/bookings.
- Document checklist, secure upload abstraction, versioning, verification/rejection, expiry, masked preview, and role-based download.
- Loan milestones, bank details, sanction amount, EMI, documents, rejection reasons, and disbursement schedule.
- Agreement/registration dates, stamp duty, registration number, legal checklist, and document status.
- Possession checklist, inspection, utility connection, key handover, possession letter, sign-off, and support transition.

### Acceptance criteria

- Legal users can verify documents but cannot see unrelated payroll data.
- Customers see only their own records in the portal.
- Sensitive documents are masked and access is audited.
- Possession cannot be delivered until configured prerequisites are satisfied.

## Phase 5 - Finance, collections, loans, and customer ledger

### Deliverables

- Payment-plan engine for booking, agreement, slab, final, GST, parking, maintenance, legal charges, and late fees.
- Receipts, partial payments, refunds, waivers, credit notes, reversals, UTR/cheque references, and PDF receipts.
- Customer ledger, receivable aging, due/overdue reminders, bank reconciliation, and collection targets.
- Finance approval thresholds and immutable approved transactions.

### Acceptance criteria

- Accounts Executive can enter drafts; Accounts Manager can approve configured transactions.
- Paid, pending, overdue, refunded, and reversed balances calculate correctly.
- Customer ledger and collection reports reconcile against transactions.

## Phase 6 - HR, attendance, payroll, and commissions

### Deliverables

- Full employee master with masked identity/bank fields and document checklist.
- Attendance, check-in/out, overtime, leave balances, holidays, and approvals.
- Salary components, incentives, sales commission rules, deductions, PF, ESIC, professional tax, advances, and loan recovery.
- Payroll draft, approval, lock/finalization, payment tracking, and salary-slip PDF.

### Acceptance criteria

- HR Executive can prepare inputs but cannot finalize payroll.
- HR Manager can approve and lock payroll.
- Locked payroll cannot be edited without controlled reversal.
- Payroll calculations have automated tests.

## Phase 7 - Vendors, procurement, bills, and petty cash

### Deliverables

- Complete vendor master with compliance documents and masked bank data.
- Purchase orders with project, material/service, quantity, rate, GST, terms, and approval status.
- Vendor bills, three-way PO matching, partial/full payment, aging, GST summaries, and vendor ledger.
- Petty-cash request -> approval -> payment -> reconciliation flow with voucher PDF and reversal entries.

### Acceptance criteria

- Vendor Manager cannot approve final payments.
- Accounts users can reconcile vendor and petty-cash balances.
- Approved vouchers and bills cannot be edited directly.
- Vendor and expense reports match source transactions.

## Phase 8 - Customer support and portal

### Deliverables

- Support ticket lifecycle with SLA timers, priority, assignment, comments, attachments, escalation, satisfaction score, and reopen flow.
- Maintenance scheduling, possession issues, documentation requests, referrals, and conversion tracking.
- Customer portal for profile, booked unit, payment schedule, receipts, documents, loan/agreement status, possession checklist, tickets, and referrals.

### Acceptance criteria

- Support users cannot see payroll, vendor banking, or unrelated finance details.
- Customers cannot see internal notes or other customers.
- SLA breach notifications and escalation are testable.

## Phase 9 - Notifications, reports, and exports

### Deliverables

- Persistent notification table with assignment, approval, reminder, due-date, overdue, document, and SLA events.
- Read/unread state, mark-all-read, deep links, preferences, and escalation rules.
- Filterable reports with pagination, saved views, date/project filters, CSV, Excel, PDF, and print output.
- Report permissions and export audit events.

### Acceptance criteria

- Notifications survive refresh and are role-scoped.
- Reports reconcile with source tables.
- Sensitive exports are blocked or masked by role.

## Phase 10 - Quality, deployment, and handover

### Deliverables

- Automated unit, repository, controller, security, integration, and workflow tests.
- Docker Compose for application, PostgreSQL, and optional object storage.
- Flyway production migrations, backup/restore notes, environment validation, and deployment documentation.
- README with architecture, setup, credentials, role matrix, API docs, seed data, screenshots, and troubleshooting.
- Final responsive visual QA on desktop, tablet, and mobile.

### Release gate

The project is considered complete only when all PDF modules have domain tables and APIs, every role has tested authorization, all required workflows have passing tests, reports reconcile, sensitive data is protected, and a clean Docker deployment succeeds from an empty database.

## Recommended execution order

Execute phases in order: 0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8 -> 9 -> 10. Do not start the next phase until the current phase's acceptance criteria are met.

## Current baseline

The repository currently has the foundation and partial implementations through the early sales/inventory/finance/vendor/support surfaces. The next implementation phase should be Phase 0 followed by Phase 1, then the full sales lifecycle in Phase 3.
