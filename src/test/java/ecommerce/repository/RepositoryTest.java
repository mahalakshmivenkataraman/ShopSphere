package ecommerce.repository;

import ecommerce.orders.Order;
import ecommerce.orders.OrderStatus;
import ecommerce.products.Product;
import ecommerce.users.Admin;
import ecommerce.users.Customer;
import ecommerce.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private ProductRepository productRepository;
    private UserRepository userRepository;
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
        userRepository = new UserRepository();
        orderRepository = new OrderRepository();
    }

    @Test
    @DisplayName("ProductRepository CRUD and Filter Operations")
    void testProductRepository() {
        Product p1 = new Product(1, "Laptop", "High end laptop", "Electronics", new BigDecimal("1200.00"), 10, true);
        Product p2 = new Product(2, "Desk Chair", "Ergonomic chair", "Furniture", new BigDecimal("150.00"), 5, true);

        productRepository.save(p1);
        productRepository.save(p2);

        assertEquals(2, productRepository.findAll().size());

        Optional<Product> found = productRepository.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());

        List<Product> electronics = productRepository.findByCategory("Electronics");
        assertEquals(1, electronics.size());
        assertEquals("Laptop", electronics.get(0).getName());

        assertTrue(productRepository.deleteById(1));
        assertFalse(productRepository.findById(1).isPresent());
        assertEquals(1, productRepository.findAll().size());
    }

    @Test
    @DisplayName("UserRepository CRUD and FindByUsername Operations")
    void testUserRepository() {
        User u1 = new Admin("U1", "admin", "adminpass");
        User u2 = new Customer("U2", "john", "johnpass", "123 Street");

        userRepository.save(u1);
        userRepository.save(u2);

        assertEquals(2, userRepository.findAll().size());

        Optional<User> foundByUsername = userRepository.findByUsername("john");
        assertTrue(foundByUsername.isPresent());
        assertEquals("U2", foundByUsername.get().getId());

        assertTrue(userRepository.deleteById("U1"));
        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    @DisplayName("OrderRepository CRUD and FindByCustomerId Operations")
    void testOrderRepository() {
        Order o1 = new Order("ORD-001", "CUST-1", new ArrayList<>(), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"), OrderStatus.CONFIRMED);
        Order o2 = new Order("ORD-002", "CUST-1", new ArrayList<>(), new BigDecimal("200.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("200.00"), OrderStatus.PENDING_PAYMENT);

        orderRepository.save(o1);
        orderRepository.save(o2);

        List<Order> custOrders = orderRepository.findByCustomerId("CUST-1");
        assertEquals(2, custOrders.size());

        Optional<Order> found = orderRepository.findById("ORD-001");
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.CONFIRMED, found.get().getStatus());
    }
}
