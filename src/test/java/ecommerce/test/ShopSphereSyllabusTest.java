package ecommerce.test;

import ecommerce.products.Product;
import ecommerce.repository.ProductRepository;
import ecommerce.repository.Repository;
import ecommerce.users.Customer;
import ecommerce.users.PremiumCustomer;
import ecommerce.users.User;
import ecommerce.users.UserRole;
import ecommerce.util.OopConceptShowcase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** JUnit 5 checks for major syllabus concepts used by ShopSphere. */
class ShopSphereSyllabusTest {
    @Test
    void inheritanceAndPolymorphismWork() {
        User user = new PremiumCustomer("U1", "premium", "p", "address", BigDecimal.TEN);
        assertTrue(user instanceof Customer);
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }

    @Test
    void genericRepositoryAndCollectionWork() {
        Repository<Product, Integer> repository = new ProductRepository();
        repository.save(new Product(1, "Keyboard", "Test", "Accessories", BigDecimal.ONE, 2, true));
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findById(1).isPresent());
        assertEquals(1, new OopConceptShowcase().countMatching(List.of("A", "B"), x -> x.startsWith("A")));
    }

    @Test
    void genericVarargsUtilityWorks() {
        assertEquals("OOP -> Generics -> JUnit", OopConceptShowcase.join("OOP", "Generics", "JUnit"));
    }
}
