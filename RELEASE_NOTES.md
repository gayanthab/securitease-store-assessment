# Release Notes — v2.0.0

## Summary
This release includes all four requested features - order lookup by ID, customer name search,
GET-endpoint performance improvements, and a new `/products` endpoint, along with a CI pipeline that
builds and publishes a Docker image. I also fixed a data-seeding issue that was already in
the baseline and found and fixed a validation issue while testing everything end to end.

I worked on each change in its own branch, tested it separately, and then merged it to
`main` in order. So the sequence below is the same order in which I actually completed
and merged the changes.

## General Assumptions
- I treated this as a real production service with real consumers and real data, since
  that is how the assignment was presented.
- I didn't add authentication/authorization because it wasn't part of the requirements
  and was outside the scope of this assignment.

## 1. Baseline fix - sequence desync
Before starting the actual tasks, I found that both `POST /customer` and `POST /order` were
failing with `duplicate key` errors. After checking the database, I found that the seed data
was inserting rows with explicit IDs, but those inserts don't update the PostgreSQL sequence
behind the columns. Because of that, new inserts could generate IDs that were already used
by the seed data.

I fixed this by adding a new Liquibase changeset that resyncs both sequences to `MAX(id)`.

**Assumption:** this only needs to run once, after the initial seed data is loaded - nothing
after this is expected to insert rows with explicit IDs.

**Branch:** `fix/customer-order-sequence-issue`

## 2. Task 1 - find order by ID
The first feature I worked on was `GET /order/{id}`. It returns the order when it exists and
returns `404` when it doesn't.

To support this, I introduced an `OrderService` because the controller was accessing the
repository directly before this change. I also added a shared `ResourceNotFoundException`
and a central `@RestControllerAdvice` for handling these errors. This was then reused by
the other tasks.

**Branch:** `feature/get-order-by-id`

## 3. Task 2 - customer search
Next I added `GET /customer?name=`.

Added GET /customer?name=, matching a substring within one word of the customer's name, not a substring of the name as a whole.

I also added a `CustomerService`, following the same structure as the `OrderService`
introduced in the previous task. This keeps the controllers consistent instead of having
the customer controller access the repository directly.

**Assumption:** the search is case-insensitive, and the value passed in the `name` parameter
is treated as one search term rather than being split into multiple terms.

**Branch:** `feature/customer-search-by-name`

## 4. Task 3 - GET endpoint performance
For this task, I wanted to understand the actual performance issue before changing the code.

I found that both list endpoints were doing a lazy-load-per-row pattern under the hood.
I changed this to use `@EntityGraph`, which reduces the database access for each endpoint.

I also added missing indexes (order.customer_id, a trigram index on customer.name)

These changes are internal and don't change the request or response structure, so existing
consumers don't need to make any changes.

I also considered pagination because it will become more important as the data grows.
However, adding it would change the payload structure and needs a product decision, so
I left it under Further Enhancements instead of including it in this release.

**Assumption:** reducing the number of database round trips was more important here, since the reported issue specifically mentioned latency
between the application and the database.

**Branch:** `fix/perf-issue-get-endpoints`

## 5. Task 4 - products endpoint
I added `/products` and introduced a many-to-many relationship between `Order` and `Product`.
`GET /order` and `GET /order/{id}` now include the products for each order. This is an
additive change, so the existing GET responses still work with the additional product data.

`POST /order` did need to change, though. It now takes `customerId` and `productIds` instead
of the old nested `customer` object. The reason is that an order now needs one or more
products, and the old request structure didn't have a suitable place to provide the
product IDs.

This means there is a breaking change to the `POST /order` request body.

**Assumption:** existing consumers of this endpoint will update their request to use the
new structure. I also assumed that existing orders created before this change will simply
have zero products. There isn't enough information to know which products should be assigned
to the existing synthetic seed orders, so I didn't try to make up that data.

**Branch:** `feature/products-endpoint`

## 6. CI pipeline
I added a GitHub Actions pipeline that runs the build, formatting and tests on every push.

Once the checks pass on `main`, the pipeline builds the Docker image and publishes it to GHCR.
Because the repository is public, the image is public as well, so no manual visibility change
was needed.

**Assumption:** The pipeline stops once the image is published, there's no CD step, so actually deploying that image
anywhere is still a manual action.

**Branch:** `feature/ci-docker-pipeline`

## 7. Request validation
After completing the main features, I tested the APIs with missing and invalid request fields
instead of assuming the existing validation was enough.

I found three endpoints that were returning a raw `500` when `name` or `description` was
blank. I added proper field-level validation so these requests return a `400` instead.

**Assumption:** validation here focuses on the request itself, such as whether a required
field is missing or blank. Checking whether a referenced customer or product actually exists
still requires a database lookup, so that behaviour was left as it was.

**Branch:** `feature/request-validation-improvements`

## Further enhancements
Identified but not built:
- Pagination on `GET /order`/`GET /customer` - this needs a product decision first, since it
  changes the payload shape.
- A caching layer.
- `UPDATE`/`DELETE` support on any resource - none exist today.
- Integration tests against a real database - the current tests are mock-based.
- Authentication/authorization - nothing exists yet.