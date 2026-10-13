# ShopSphere — Final Integrated OOP Mini Project

**Java Swing E-Commerce Management System**

## Main features

- Customer, Seller and Admin roles
- 108 seeded products across 9 categories
- Existing products distributed across **Seller 1, Seller 2 and Seller 3**
- Product images, product details and related-product suggestions
- Seller image upload for newly added products
- Seller/admin stock editing for products they are allowed to manage; stock updates persist
- Per-product locking prevents two threads in one running application from selling the same final unit
- Persistent users, carts, products and orders using Java serialization
- INR (₹) pricing
- Dark and Light themes
- Cart and multi-user cart visibility for Admin
- Checkout review → payment screen → simulated payment processing
- Customer **Waiting Orders** and **Completed Orders**
- Seller **Orders for My Products** with **Dispatch Order** and **Cancel Order**
- Seller-specific fulfillment state for multi-seller orders
- Stock restoration when a seller/customer cancels an eligible order
- Admin system-wide order and inventory monitoring
- Generic repository and generic file persistence
- Custom checked exceptions
- Multithreading for payment, inventory and notifications
- JUnit 5 test classes plus dependency-free test runners

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Customer | `Alex` | `password123` |
| Seller | `seller1` | `seller123` |
| Seller | `seller2` | `seller123` |
| Seller | `seller3` | `seller123` |
| Admin | `admin` | `admin123` |

## Customer flow

1. Log in as `Alex`.
2. Browse the catalog and open a product to view its details and seller.
3. Add products to the cart.
4. Checkout → review order → payment screen → simulated payment.
5. The order appears under **Waiting Orders**.
6. Log in as the relevant seller.
7. The seller sees the order in **Orders for My Products**.
8. **Dispatch Order** changes that seller's portion to dispatched.
9. When all active sellers dispatch their portions, the customer's order moves to **Completed Orders**.
10. If a seller cannot fulfill its portion, **Cancel Order** restores that portion's stock and updates the customer's order state.

## Inventory concurrency

Checkout performs the stock check and decrement while holding a lock for that product. The concurrency runner tests 20 simultaneous attempts against five units and two simultaneous customers competing for one final unit. Only one final-unit purchase should succeed. Sellers can select a product in their inventory table and choose **Update Selected Product Stock**; negative quantities are rejected and the new quantity is saved to `data/products.dat`.

**Deployment limitation:** this version uses local Java-serialized files and in-memory repositories. Two separate computers will each have their own data if they simply run the desktop app independently. A shared hosted test requires a common backend/API and database with a database transaction/row lock or atomic conditional stock update. See `DEPLOYMENT_PLAN.md`.

## Persistence

The `data/` folder stores application data. Keep it when testing logout/login or restarting the application. The application reuses the same user, cart, product and order records instead of creating a fresh session every time.

## Windows execution

The batch scripts use **project-relative Java source paths**, so the project can be stored inside a directory containing spaces such as:

`C:\Users\KHUSHI\Desktop\oops mini project\ShopSphere_Final`

### Run

Double-click `run.bat` or run it from Command Prompt inside the `ShopSphere_Final` folder.

### Tests

Run `test.bat` to compile the main project and execute the dependency-free integration test runners.

## JDK

JDK 17+ is supported. The project is configured for Java 17 source/target compatibility, so JDK 21 and JDK 25 are also suitable.

## Important academic note

This is a college OOP prototype. Payment is simulated and no real payment gateway is connected.
