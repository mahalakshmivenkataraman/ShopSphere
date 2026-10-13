# OOP / Course Concept Audit

| Concept | Evidence in project |
|---|---|
| Classes and objects | User, Product, Order, Cart, GUI frames and services |
| Encapsulation | Private fields with getters/setters in domain models |
| Inheritance | Customer, Seller, Admin and PremiumCustomer extend User |
| Runtime polymorphism | Role-specific User subclasses; Runnable/Callable tasks |
| Abstraction | Abstract User class; Repository and InventoryService interfaces |
| Interfaces | Repository<T, ID>, InventoryService |
| Generics | Repository<T, ID>, generic FileManager save/load methods |
| Collections | ArrayList, List, Map, ConcurrentHashMap, streams |
| Exception handling | Custom application/persistence exceptions; try/catch/finally |
| Packages | ecommerce.gui, users, products, orders, repository, persistence, etc. |
| Static and instance members | Static catalog helpers and instance state across models/services |
| Constructors | Default, parameterized, and overloaded constructors |
| Multithreading | Threaded notification, callable payment simulation, concurrent inventory tests |
| Synchronization / locking | ReentrantLock per product and file lock |
| File persistence | Serialized `.dat` files, temporary file + replace |
| GUI | Swing login, registration, product, cart, order and inventory panels |

**Scope note:** payment is simulated. This is a classroom prototype, not a production commerce/security system.
