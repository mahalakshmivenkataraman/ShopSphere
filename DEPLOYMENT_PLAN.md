# ShopSphere deployment plan (for two-computer stock testing)

## Current version
ShopSphere is a Java Swing desktop application. Product and order data are stored in local serialized files under `data/`. Uploading the ZIP or running two copies of the current application does **not** share stock between computers. Each copy would have separate local state, so it cannot prove cross-computer locking.

## Recommended architecture
Keep Swing as the user interface, but move shared operations to one hosted backend:

1. **Swing client on each computer** — login, browse products, seller dashboard, cart and checkout screens.
2. **Java backend/API** — validates login and seller ownership; exposes product, stock, and order endpoints. Checkout must happen here, not only in the Swing client.
3. **Shared PostgreSQL database** — stores users, products, stock quantities and orders.
4. **Atomic checkout** — inside one database transaction, reserve/decrement stock conditionally (for example, update a product only when `stock >= requestedQuantity`) and create the order. If any item cannot be reserved or payment simulation fails, roll back/restore consistently. A database row lock (`SELECT ... FOR UPDATE`) or conditional `UPDATE` is required; Java `synchronized` alone cannot coordinate different server processes.

## Student-friendly hosting option
- Host the Java backend on a Java-capable application host such as Render or another platform that supports a persistent Java service.
- Use a managed PostgreSQL database from the same provider or another managed database provider.
- Configure the backend's database URL, username and password through environment variables/secrets, not source code.
- Build the Swing app as a runnable JAR for each computer and configure its API base URL to the hosted backend URL.
- Allow HTTPS access to the API and restrict database network access to the backend.

## Test after the backend exists
1. Set one product's stock to `1` using a seller account.
2. Open the packaged Swing client on Computer A and Computer B, logging in as two different customers.
3. Add the same product to both carts.
4. Start checkout on both computers as close together as possible.
5. Expected result: one order succeeds, the other gets an out-of-stock response, the database stock remains `0`, and only one order line for that final unit is accepted.
6. Log in as the seller and confirm the shared stock and order state.

## Important scope note
This deployment requires adding a backend/API and replacing or migrating the local file-based repositories to shared database-backed repositories. It is not achieved by deploying the existing Swing GUI alone. The current in-process per-product lock protects simultaneous threads in one running application only; it does not coordinate two independent JVMs or machines.
