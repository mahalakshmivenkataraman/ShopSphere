package ecommerce.persistence;

import ecommerce.products.Product;
import ecommerce.users.Admin;
import ecommerce.users.User;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class FileManagerTestRunner {

    public static void runTests() {
        String testDir = "target/test_data_runner";
        File dir = new File(testDir);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
            dir.delete();
        }

        FileManager fm = new FileManager(testDir);

        // Test 1: Load non-existent file returns empty list
        List<Product> products = fm.loadProducts();
        if (products == null || !products.isEmpty()) throw new AssertionError("Expected empty product list for non-existent file");

        // Test 2: Save and reload products
        Product p1 = new Product(10, "Test Laptop", "Spec", "Tech", new BigDecimal("500.00"), 5, true);
        fm.saveProducts(List.of(p1));

        List<Product> reloadedProducts = fm.loadProducts();
        if (reloadedProducts.size() != 1 || !"Test Laptop".equals(reloadedProducts.get(0).getName())) {
            throw new AssertionError("Save/load products failed");
        }

        // Test 3: Save and reload users
        User u1 = new Admin("A1", "admin_user", "password");
        fm.saveUsers(List.of(u1));

        List<User> reloadedUsers = fm.loadUsers();
        if (reloadedUsers.size() != 1 || !"admin_user".equals(reloadedUsers.get(0).getUsername())) {
            throw new AssertionError("Save/load users failed");
        }
    }
}
