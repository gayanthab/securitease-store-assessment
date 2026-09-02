# Release Notes — v2.0.0

## Summary
Delivers all four requested features — order lookup by ID, customer name search,
GET-endpoint performance optimizations, and a new `/products` endpoint — plus a CI
pipeline that builds and publishes a Docker image. Also fixes a pre-existing issue in
the baseline application, and adds further optimizations and validation enhancements
found during review.

Each item below — the baseline fix, all four tasks, the CI pipeline, and the
validation enforcement — was implemented on its own branch, tested independently, and
merged to `main` in sequence, in the order listed.

## General Assumptions
- Treated as a live production service with real API consumers and real production
  data, per the assignment's own framing.
- Where a requirement was ambiguous, a decision was made and documented rather than
  guessed silently.
- No authentication/authorization was added — out of scope for this assignment.

## 1. Baseline Fix — Sequence Desync
- **Fix:** `POST /customer` and `POST /order` failed with `duplicate key` errors. Root
  cause: seed data used explicit IDs, which never advanced the underlying Postgres
  sequence. Fixed with a new Liquibase changeset resyncing both sequences to `MAX(id)`.
- **Assumptions:** the fix only needs to run once, right after the bulk seed load — no
  seed data is added with explicit IDs afterward.
- **Branch:** `fix/customer-order-sequence-issue`

## 2. Task 1 — Find Order by ID
- **Fix:** Added `GET /order/{id}`, returning `404` if not found. Introduced an
  `OrderService` layer (the controller previously called the repository directly),
  plus a shared exception-handling pattern (`ResourceNotFoundException` + a central
  handler) — both reused by every later task.
- **Assumptions:** none beyond the requirement itself.
- **Branch:** `feature/get-order-by-id`

## 3. Task 2 — Customer Search
- **Fix:** Added `GET /customer?name=`, matching a substring within one word of the
  customer's name — not a substring of the name as a whole. Also introduced a
  `CustomerService` layer (the controller previously called the repository directly)
  — a deliberate design change matching the pattern already established for `Order`
  in Task 1, so the codebase has a consistent place for business logic ahead of future
  changes (`Product` follows the same pattern in Task 4).
- **Assumptions:** search is case-insensitive, and the query is treated as a single
  term, not split into multiple words.
- **Branch:** `feature/customer-search-by-name`

## 4. Task 3 — GET Endpoint Performance
- **Fix:** Fixed N+1 query patterns (`@EntityGraph`) on the list endpoints, and added
  missing indexes (`order.customer_id`, a trigram index on `customer.name`). This
  is a purely internal change — no request or response shape changed anywhere, so
  there's no consumer disruption and no contract change. Pagination was also
  considered for this issue, since it becomes the more important optimization at real
  scale, but it inherently requires consumer coordination — tracked as a Further Enhancement instead of built here.
- **Assumptions:** minimizing round trips was prioritized, since the reported issue specifically cites high latency between the app and the database.
- **Branch:** `fix/perf-issue-get-endpoints`

## 5. Task 4 — Products Endpoint
- **Fix:** Added `/products` (`POST`, `GET` all + by ID), many-to-many with `Order`.
  `GET /order` and `GET /order/{id}` responses now additionally include each order's
  products. `POST /order`'s **request body** now requires `customerId`/`productIds` instead of a nested `customer` object, since "an
  order contains 1 or more products" makes this a mandatory concept the old shape had
  no place for. This is a deliberate breaking change, scoped only to that one request
  body — no `GET` response changed shape.
- **Assumptions:**
  - Consumers of the order-creation endpoint are already aware of this change and will
    align their integration accordingly.
  - Existing, already-seeded orders keep zero products — no retroactive backfill.
- **Branch:** `feature/products-endpoint`

## 6. CI Pipeline
- **Fix:** GitHub Actions builds, format-checks, and tests every push; on merge to
  `main`, builds and publishes a Docker image to GitHub Container Registry. Since the
  source repository is public, the published image is public too.
- **Assumptions:** no database service is needed in CI, since every test mocks the
  repository/service layer rather than hitting a real one.
- **Branch:** `feature/ci-docker-pipeline`

## 7. Request Validation Enforcement
- **Fix:** Closed a gap found during review and test: blank/missing required fields on
  all three create endpoints crashed with `500` instead of a clean `400`, and
  duplicate `productIds` were incorrectly rejected as "not found." Added field-level
  validation and fixed the duplicate-ID logic.
- **Assumptions:** validation covers request *shape* only (blank/missing fields) —
  existence checks (does this customer/product actually exist) stay separate, since
  they need a database lookup that request validation can't perform.
- **Branch:** `feature/request-validation-improvements`

## Further Enhancements
- Pagination on `GET /order`/`GET /customer` — deferred pending a product decision,
  since requires consumer coordination.
- A caching layer, `UPDATE`/`DELETE` endpoints on all resources, integration-test
  infrastructure (Testcontainers), and authentication/authorization.
