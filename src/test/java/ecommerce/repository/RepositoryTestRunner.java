package ecommerce.repository;

import ecommerce.orders.Order;
import ecommerce.orders.OrderStatus;
import ecommerce.products.Product;
import ecommerce.users.Admin;
import ecommerce.users.Customer;
import ecommerce.users.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryTestRunner {

    public static void runTests() {
        testProductRepository();
        testUserRepository();
        testOrderRepository();
    }

    private static void testProductRepository() {
        ProductRepository repo = new ProductRepository();
        Product p1 = new Product(1, "Laptop", "High end laptop", "Electronics", new BigDecimal("1200.00"), 10, true);
        Product p2 = new Product(2, "Desk Chair", "Ergonomic chair", "Furniture", new BigDecimal("150.00"), 5, true);

        repo.save(p1);
        repo.save(p2);

        if (repo.findAll().size() != 2) throw new AssertionError("Expected 2 products in repo");

        Optional<Product> found = repo.findById(1);
        if (found.isEmpty() || !"Laptop".equals(found.get().getName())) throw new AssertionError("Product findById failed");

        List<Product> electronics = repo.findByCategory("Electronics");
        if (electronics.size() != 1 || !"Laptop".equals(electronics.get(0).getName())) throw new AssertionError("FindByCategory failed");

        if (!repo.deleteById(1)) throw new AssertionError("DeleteById failed");
        if (repo.findById(1).isPresent()) throw new AssertionError("Deleted product still present");
    }

    private static void testUserRepository() {
        UserRepository repo = new UserRepository();
        User u1 = new Admin("U1", "admin", "adminpass");
        User u2 = new Customer("U2", "john", "johnpass", "123 Street");

        repo.save(u1);
        repo.save(u2);

        if (repo.findAll().size() != 2) throw new AssertionError("Expected 2 users");

        Optional<User> found = repo.findByUsername("john");
        if (found.isEmpty() || !"U2".equals(found.get().getId())) throw new AssertionError("FindByUsername failed");

        if (!repo.deleteById("U1")) throw new AssertionError("DeleteById failed for user");
    }

    private static void testOrderRepository() {
        OrderRepository repo = new OrderRepository();
        Order o1 = new Order("ORD-001", "CUST-1", new ArrayList<>(), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"), OrderStatus.CONFIRMED);
        Order o2 = new Order("ORD-002", "CUST-1", new ArrayList<>(), new BigDecimal("200.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("200.00"), OrderStatus.PENDING_PAYMENT);

        repo.save(o1);
        repo.save(o2);

        List<Order> custOrders = repo.findByCustomerId("CUST-1");
        if (custOrders.size() != 2) throw new AssertionError("Expected 2 orders for customer");

        Optional<Order> found = repo.findById("ORD-001");
        if (found.isEmpty() || found.get().getStatus() != OrderStatus.CONFIRMED) throw new AssertionError("Order findById failed");
    }
}
