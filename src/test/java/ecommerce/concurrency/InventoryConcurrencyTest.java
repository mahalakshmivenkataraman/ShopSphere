package ecommerce.concurrency;

import ecommerce.exceptions.InsufficientStockException;
import ecommerce.products.Product;
import ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InventoryConcurrencyTest {

    private ProductRepository productRepository;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
        inventoryService = new InventoryServiceImpl(productRepository, null);
    }

    @Test
    @DisplayName("Concurrent stock check and decrement prevents negative stock")
    void testConcurrentStockCheckAndDecrement() throws InterruptedException {
        int productId = 999;
        int initialStock = 5;
        Product p = new Product(productId, "Limited Item", "Description", "Category", new BigDecimal("100.00"), initialStock, true);
        productRepository.save(p);

        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    inventoryService.decreaseStock(productId, 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Product updatedProduct = productRepository.findById(productId).orElseThrow();

        assertEquals(5, successCount.get(), "Exactly 5 purchases should succeed");
        assertEquals(15, failureCount.get(), "Exactly 15 purchases should fail due to insufficient stock");
        assertEquals(0, updatedProduct.getStock(), "Final stock should be exactly 0 and never negative");
    }
}
