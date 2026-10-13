# Integration Report

## Scope

This version builds on the latest ShopSphere integrated version and adds the requested seller/order coherence workflow while retaining the earlier product images, INR pricing, light/dark themes, product details, related products, seller uploads, persistent carts and payment-review flow.

## Major changes

1. Added Seller 2 and Seller 3 demo accounts.
2. Distributed the seeded catalog across three sellers when ownership is missing.
3. Added seller ownership to product details and catalog cards.
4. Added per-seller fulfillment state inside an order.
5. Added seller dispatch and seller cancellation operations.
6. Added customer Waiting Orders and Completed Orders tabs.
7. Added seller-specific order visibility.
8. Rebound persisted order line products to the current product repository during startup so older orders also recover current seller ownership.
9. Added stock restoration for seller cancellation.
10. Added syllabus-focused generic/collection/OOP demonstration code and tests.
11. Kept the Windows launch scripts project-relative to avoid path-with-spaces failures.

## Multi-seller order rule

A single customer order can contain products from multiple sellers. Each seller controls only its own portion of the order. The customer order becomes completed only when every active seller portion has been dispatched. If all seller portions are cancelled, the overall order becomes cancelled.

## Verification performed in this environment

- Main Java source compilation: PASS
- Dependency-free repository test: PASS
- Dependency-free file persistence test: PASS
- Inventory concurrency test: PASS
- Cart/order coherence test: PASS
- Multi-seller dispatch workflow test: PASS
- Seller cancellation + stock restoration test: PASS
- OOP syllabus concept test: PASS
- `Main --demo`: PASS

Maven itself was not available in the verification environment, so the JUnit 5 suite was not executed here. The JUnit test sources and Maven configuration remain included for local execution.
