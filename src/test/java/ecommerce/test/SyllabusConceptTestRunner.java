package ecommerce.test;

import ecommerce.products.Product;
import ecommerce.repository.ProductRepository;
import ecommerce.repository.Repository;
import ecommerce.users.PremiumCustomer;
import ecommerce.users.User;
import ecommerce.util.OopConceptShowcase;

import java.math.BigDecimal;
import java.util.List;

/** Dependency-free checks for the OOP syllabus examples included in the project. */
public final class SyllabusConceptTestRunner {
    private SyllabusConceptTestRunner() { }

    public static void runTests() {
        User user = new PremiumCustomer("U1", "premium", "password", "Address", BigDecimal.TEN);
        require(user instanceof PremiumCustomer, "Inheritance should create a PremiumCustomer object");
        require("CUSTOMER".equals(user.getRoleString()), "Polymorphic user role should remain CUSTOMER");

        Repository<Product, Integer> repository = new ProductRepository();
        repository.save(new Product(1, "Keyboard", "Test", "Accessories", BigDecimal.ONE, 2, true));
        require(repository.findById(1).isPresent(), "Generic repository lookup should work");

        OopConceptShowcase showcase = new OopConceptShowcase();
        require(showcase.countMatching(List.of("A", "B"), value -> value.startsWith("A")) == 1, "Generic method should work");
        require("OOP -> Generics -> JUnit".equals(OopConceptShowcase.join("OOP", "Generics", "JUnit")), "Varargs utility should work");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
