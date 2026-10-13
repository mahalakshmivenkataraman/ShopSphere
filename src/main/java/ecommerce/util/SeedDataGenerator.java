package ecommerce.util;

import ecommerce.persistence.FileManager;
import ecommerce.products.Product;
import ecommerce.repository.ProductRepository;
import ecommerce.repository.UserRepository;
import ecommerce.users.Admin;
import ecommerce.users.Customer;
import ecommerce.users.Seller;

import java.math.BigDecimal;

public class SeedDataGenerator {

    public static void initializeSeedData(UserRepository userRepo, ProductRepository productRepo, FileManager fileManager) {
        // Always ensure the demo roles exist, even when an older data file already exists.
        if (userRepo.findByUsername("admin").isEmpty()) userRepo.save(new Admin("USR-001", "admin", "admin123"));
        if (userRepo.findByUsername("seller1").isEmpty()) userRepo.save(new Seller("USR-002", "seller1", "seller123", "TechStore Pro"));
        if (userRepo.findByUsername("seller2").isEmpty()) userRepo.save(new Seller("USR-005", "seller2", "seller123", "SmartChoice Store"));
        if (userRepo.findByUsername("seller3").isEmpty()) userRepo.save(new Seller("USR-006", "seller3", "seller123", "Everyday Hub"));
        if (userRepo.findByUsername("customer1").isEmpty()) userRepo.save(new Customer("USR-003", "customer1", "customer123", "123 Main St, Tech City"));
        if (userRepo.findByUsername("john_doe").isEmpty()) userRepo.save(new Customer("USR-004", "john_doe", "pass123", "456 Oak Ave, Silicon Valley"));
        if (fileManager != null) fileManager.saveUsers(userRepo.findAll());

        // Initialize products if empty
        if (productRepo.findAll().isEmpty()) {
            productRepo.save(new Product(101, "UltraBook Laptop", "15-inch Intel i7 16GB RAM 512GB SSD", "Electronics", new BigDecimal("999.99"), 15, true));
            productRepo.save(new Product(102, "Smartphone X", "6.5-inch OLED Display 128GB", "Electronics", new BigDecimal("699.50"), 25, true));
            productRepo.save(new Product(103, "Noise Cancelling Headphones", "Wireless Over-Ear ANC Headphones", "Audio", new BigDecimal("199.99"), 40, true));
            productRepo.save(new Product(104, "Ergonomic Wireless Mouse", "Dual-mode Bluetooth & 2.4G mouse", "Accessories", new BigDecimal("49.95"), 50, true));
            productRepo.save(new Product(105, "RGB Mechanical Keyboard", "Tactile Blue Switches RGB Backlit", "Accessories", new BigDecimal("89.99"), 30, true));

            if (fileManager != null) {
                fileManager.saveProducts(productRepo.findAll());
            }
            System.out.println("[SeedDataGenerator] Sample products initialized.");
        }

        // Existing catalog products are distributed across three demo sellers.
        java.util.List<Product> products = productRepo.findAll();
        String[] sellerIds = {"USR-002", "USR-005", "USR-006"};
        String[] sellerNames = {"seller1", "seller2", "seller3"};
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.getSellerId() == null || product.getSellerId().isBlank()) {
                int idx = i % sellerIds.length;
                product.setSellerId(sellerIds[idx]);
                product.setSellerName(sellerNames[idx]);
            }
        }
        if (fileManager != null) fileManager.saveProducts(productRepo.findAll());
    }
}
