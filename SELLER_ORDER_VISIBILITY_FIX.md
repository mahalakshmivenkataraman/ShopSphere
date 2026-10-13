# Seller Order Visibility Fix

## Problem fixed
Orders visible to Admin were not always visible in the corresponding Seller dashboard because older persisted products could identify a seller by username (`seller1`) while the current logged-in seller session used a user ID (`USR-002`).

## Changes
- Seller order lookup now matches seller user ID, username, and persisted product seller identity.
- Seller dispatch/cancel actions resolve the correct persisted seller key before changing fulfillment status.
- Seller order tables show the products belonging to the logged-in seller instead of relying only on the full customer order line list.
- Existing saved orders remain compatible; no manual data migration is required.
- Added a regression test for the `USR-002` / `seller1` identity mismatch.

## Verification
All 6 dependency-free integration test groups passed:
1. RepositoryTest
2. FileManagerTest
3. InventoryConcurrencyTest
4. ShopSphereCoherenceTest
5. SellerWorkflowTest
6. SyllabusConceptTest

The Java main source was also compiled successfully with `javac`.
