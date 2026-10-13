package ecommerce.persistence;

import ecommerce.products.Product;
import ecommerce.users.Admin;
import ecommerce.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {

    private static final String TEST_DATA_DIR = "target/test_data";
    private FileManager fileManager;

    @BeforeEach
    void setUp() {
        // Clean up test data directory before each test
        File dir = new File(TEST_DATA_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            dir.delete();
        }
        fileManager = new FileManager(TEST_DATA_DIR);
    }

    @Test
    @DisplayName("Load from non-existent file returns empty list")
    void testLoadNonExistentFile() {
        List<Product> products = fileManager.loadProducts();
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    @DisplayName("Save and reload products via atomic .dat serialization")
    void testSaveAndLoadProducts() {
        Product p1 = new Product(10, "Test Laptop", "Spec", "Tech", new BigDecimal("500.00"), 5, true);
        fileManager.saveProducts(List.of(p1));

        List<Product> reloaded = fileManager.loadProducts();
        assertEquals(1, reloaded.size());
        assertEquals("Test Laptop", reloaded.get(0).getName());
        assertEquals(5, reloaded.get(0).getStock());
    }

    @Test
    @DisplayName("Save and reload users via atomic serialization")
    void testSaveAndLoadUsers() {
        User u1 = new Admin("A1", "admin_user", "password");
        fileManager.saveUsers(List.of(u1));

        List<User> reloaded = fileManager.loadUsers();
        assertEquals(1, reloaded.size());
        assertEquals("admin_user", reloaded.get(0).getUsername());
    }
}
