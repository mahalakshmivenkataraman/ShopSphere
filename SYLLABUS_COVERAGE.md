# OOP Syllabus Coverage — ShopSphere

## Unit I — Introduction

| Topic | Coverage | Where |
|---|---|---|
| OOP basics | ✅ | Domain model classes and services |
| Java data types / variables / operators | ✅ | Throughout project |
| Control structures | ✅ | Services, GUI, validation |
| Methods | ✅ | All layers |
| Method overloading | ✅ | `Product`, `User`, `CouponService` |
| Math class | ✅ | `MainApp` product-image fallback / calculations |
| Arrays | ✅ | Category/product/seller arrays |
| Objects and classes | ✅ | All domain classes |
| Constructors | ✅ | Domain classes and services |
| Visibility modifiers | ✅ | `private`, `public`, `protected` through project |
| `this` reference | ✅ | Constructors and setters |
| Static members | ✅ | `AppData`, `MainApp`, utilities |
| Initialization blocks | ✅ | `AppData`, `MainApp`, `OopConceptShowcase` |
| Finalizer | ⚠️ Not used | Deprecated/obsolete in modern Java; intentionally avoided |

## Unit II — Inheritance and Polymorphism

| Topic | Coverage |
|---|---|
| Inheritance | ✅ `Customer`, `Seller`, `Admin`, `PremiumCustomer` |
| Super/subclass | ✅ |
| Single inheritance | ✅ |
| Multilevel inheritance | ✅ `User → Customer → PremiumCustomer` |
| Hierarchical inheritance | ✅ `User → Customer/Seller/Admin` |
| Multiple inheritance | ✅ Achieved through interfaces, as Java does not support multiple class inheritance |
| Constructor chaining | ✅ |
| Method overriding | ✅ Through inherited/overridden domain behavior |
| Polymorphism / dynamic binding | ✅ `User` references can refer to subclass objects |
| Abstract class | ✅ `User` |
| Interfaces | ✅ `Repository`, `InventoryService` |

## Unit III — Exception Handling and Packages

| Topic | Coverage |
|---|---|
| Exception benefits / hierarchy | ✅ |
| Checked and unchecked exceptions | ✅ Custom checked persistence/business exceptions plus runtime validation exceptions |
| `try` / `catch` | ✅ |
| `throw` | ✅ |
| `throws` | ✅ |
| `finally` | ✅ File locking and inventory operations |
| User-defined exception subclasses | ✅ Many `ecommerce.exceptions` classes |
| Packages | ✅ `ecommerce.*` package structure |
| File operations | ✅ Java serialization and `java.nio.file` |
| File streams | ✅ `FileInputStream`, `FileOutputStream`, buffered streams |

## Unit IV — Generics and Collections

| Topic | Coverage |
|---|---|
| Generic classes/interfaces | ✅ `Repository<T, ID>` |
| Generic methods | ✅ `FileManager.saveListToFile`, `loadListFromFile`, `OopConceptShowcase.countMatching` |
| Generic restrictions | ✅ Repository uses typed IDs/entities |
| List / ArrayList | ✅ |
| LinkedList | ✅ OOP showcase checkout queue |
| Set / TreeSet | ✅ OOP showcase |
| Queue | ✅ OOP showcase |
| Iterator | ✅ OOP showcase |
| Map / HashMap / ConcurrentHashMap | ✅ |
| Collections / streams | ✅ Product and order filtering |

## Unit V — Multithreading and JUnit

| Topic | Coverage |
|---|---|
| Thread lifecycle/basic methods | ✅ Payment/notification tasks and thread execution |
| Runnable | ✅ `InventoryTask`, `NotificationTask` |
| Callable | ✅ `PaymentTask` |
| Thread synchronization | ✅ `ReentrantLock` and synchronized cart access |
| Lambda expressions | ✅ GUI listeners, streams and predicates |
| Functional interface | ✅ `Predicate`, `Callable`, `Runnable` |
| Var-args | ✅ `OopConceptShowcase.join(String...)` |
| JUnit introduction | ✅ JUnit 5 dependencies and test class |
| JUnit lifecycle API | ✅ Existing JUnit test classes use lifecycle annotations |
| JUnit 5 application testing | ✅ `ShopSphereSyllabusTest` and existing tests |
| Dependency injection | ✅ `OrderService.setOrderRepository(...)`, inventory/repository injection |
| Mocking | ⚠️ Not added | No mocking framework was necessary for this prototype |
| Testing traits/grouping | ⚠️ Partially | Dependency-free grouped test runner plus JUnit tests |

## Additional e-commerce workflow implemented

- Three-seller catalog ownership
- Seller-specific order routing
- Seller dispatch/cancellation
- Customer waiting/completed order separation
- Multi-seller order state aggregation
- Stock restoration for cancelled seller portions
- Persistent cart/order state across logout/login/restart
