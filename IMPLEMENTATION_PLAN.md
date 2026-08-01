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

## Current implementation status

### Latest verified snapshot — Phase 8

- **Phase 0 — Complete:** H2 and PostgreSQL migrations, API error envelopes, correlation IDs, health probes, OpenAPI metadata, and build verification are implemented.
- **Phase 1 — Complete:** Super Admin administration, RBAC denial responses, audit events, login throttling, refresh-token rotation/reuse rejection, session revocation, logout, and security tables are implemented and verified.
- **Phase 2 — Complete:** Inventory projects, wings, floors, units, pricing/status history, reservation expiry, conflict protection, role-scoped reads, filters, CSV export, and floor-plan UI are implemented and smoke-tested.
- **Phase 3 — In progress:** Sales qualification, duplicate checks, assignment history/transfer, timelines, follow-ups, site visits, negotiation approvals, quotation versions, booking payment validation, and unit conflict prevention are implemented. Richer quotation/approval/transfer actions and automated workflow tests remain.
- **Phase 4 — In progress:** Customer records, masked documents and verification, loan milestones, agreements/registration, possession cases, readiness gates, checklists, sign-off, and seeded post-booking data are implemented and smoke-tested. Customer portal isolation, object storage, and automated tests remain.
- **Phase 5 — In progress:** Installments, partial receipts, customer ledger/aging, reversals, bank entries, collection targets, finance reports, seeded data, and the finance workspace are connected. Automated finance workflow tests remain.
- **Phase 6 — In progress:** Employees, attendance, leave, payroll calculation, salary components, payroll locking, salary payment status, seeded HR data, and HR workspace views are connected. Automated payroll tests remain.
- **Phase 7 — In progress:** Vendor compliance, purchase orders, approvals, vendor bills, partial payments, vendor ledger, petty-cash request/approval/payment/reversal, seeded records, and procurement workspace views are connected. Automated procurement tests remain.
- **Phase 8 — In progress:** Support tickets, SLA/priority tracking, assignment, comments, satisfaction capture, maintenance scheduling/status, referrals, referral conversion, seeded after-sales data, support workspace views, CSV export, backend compilation, frontend build, and API smoke tests are complete. Customer portal isolation, persistent notification escalation, and automated support tests remain.
- **Phase 9 — Pending:** Persistent role-scoped notifications, escalation rules, filterable reports, Excel/PDF exports, saved views, and export auditing.
- **Phase 10 — Pending:** Full automated test suite, Docker deployment validation, backup/restore notes, final documentation, and responsive visual QA.

### Verified local runtime

- Backend compiled successfully with Maven and is running at `http://127.0.0.1:8080/api/v1`.
- Frontend production build completed successfully and Vite is running at `http://127.0.0.1:5173/`.
- Phase 8 smoke test passed for dashboard, tickets, comments, ticket status, maintenance status, seeded referrals, and Finance-role access denial.

### Overall release status

The application is functional for the implemented role-based modules, but it is not yet the final enterprise release. Phases 3–8 still have acceptance-gate work, while Phases 9–10 remain pending.

### Historical phase notes

- **Phase 0 complete:** local H2 and production PostgreSQL migration tracks, API error envelope, correlation IDs, health probes, OpenAPI metadata, and build verification are in place.
- **Phase 1 complete:** Super Admin administration APIs, role denial responses, audit events, login throttling, refresh-token rotation with reuse rejection, session revocation, logout, and security migration tables are working and verified.
- **Phase 2 complete:** normalized inventory control tables, project master create/update APIs, wing/floor APIs, unit create/update APIs, price/status history, reservation expiry, reserve/release conflict protection, role-scoped inventory reads, filters, CSV export, and the dedicated floor-plan inventory UI are implemented and smoke-tested.
- **Phase 3 in progress:** sales lifecycle APIs and migrations now cover qualification scoring, duplicate checks, assignment history/transfer, activity timeline, follow-ups, site visits, negotiation approvals, quotation versions, booking payment validation, and unit conflict prevention. Role-aware sales workspace screens are wired for qualification, follow-ups, site visits, negotiations, and bookings; richer quotation/approval/transfer UI actions are the remaining Phase 3D work.
- **Phase 4 in progress:** customer records, masked document metadata and verification, loan milestones, agreement/registration tracking, possession cases, readiness gates, checklist completion, sign-off, and seeded post-booking records are implemented and smoke-tested. UI panels are wired for documents, loans, agreements, and possession; binary object storage, a separate Legal role, and a customer portal remain governed by later scope/role decisions.
- **Phase 5 in progress:** finance tables and APIs now cover installment plans, partial receipts, customer ledger and aging, refunds/reversals, bank entries, collection targets, and finance reports. Seeded collection data and the finance workspace are connected; automated finance workflow tests remain before the phase release gate.
- **Phase 6 in progress:** HR tables and APIs now cover attendance, holidays/leave requests, payroll calculations, salary components, payroll locking, salary payment status, and seeded HR records. The HR workspace is connected for employee, attendance, leave, payroll, and salary views; automated payroll calculation/locking tests remain before the phase release gate.
- **Phase 7 in progress:** vendor compliance fields, purchase orders, approval status, vendor bills, partial payments, vendor ledger, and petty-cash request→approval→payment→reversal controls are implemented with seeded records. Vendor/procurement workspace views are connected; automated procurement and petty-cash tests remain before the phase release gate.
- **Phase 8 in progress:** support ticket lifecycle, SLA/priority tracking, assignment, comments, satisfaction capture, maintenance scheduling/status, referrals, and referral conversion APIs are implemented with seeded after-sales records. Support workspace views and exports are connected; customer portal isolation, persistent notification escalation, and automated support workflow tests remain before the phase release gate.
- **Remaining:** Phases 3–10 remain partial or pending; the current product must not be represented as a fully complete enterprise ERP until those workflows and release gates pass.
