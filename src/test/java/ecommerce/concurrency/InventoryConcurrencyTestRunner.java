package ecommerce.concurrency;

import ecommerce.exceptions.InsufficientStockException;
import ecommerce.products.Product;
import ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryConcurrencyTestRunner {

    public static void runTests() {
        ProductRepository productRepository = new ProductRepository();
        InventoryService inventoryService = new InventoryServiceImpl(productRepository, null);

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

        try {
            latch.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Product updatedProduct = productRepository.findById(productId).orElseThrow();

        if (successCount.get() != 5) throw new AssertionError("Expected 5 successes, got " + successCount.get());
        if (failureCount.get() != 15) throw new AssertionError("Expected 15 failures, got " + failureCount.get());
        if (updatedProduct.getStock() != 0) throw new AssertionError("Expected final stock 0, got " + updatedProduct.getStock());

        // Reproduce the requested race: two customers attempt to buy the final single unit.
        int lastUnitProductId = 1000;
        productRepository.save(new Product(lastUnitProductId, "Last Unit Item", "Description", "Category",
                new BigDecimal("25.00"), 1, true));
        CountDownLatch startTogether = new CountDownLatch(1);
        CountDownLatch bothFinished = new CountDownLatch(2);
        AtomicInteger lastUnitSuccess = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();
        ExecutorService twoCustomers = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            twoCustomers.submit(() -> {
                try {
                    startTogether.await();
                    inventoryService.decreaseStock(lastUnitProductId, 1);
                    lastUnitSuccess.incrementAndGet();
                } catch (InsufficientStockException expected) {
                    outOfStock.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    bothFinished.countDown();
                }
            });
        }
        startTogether.countDown();
        try {
            bothFinished.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Concurrency test interrupted", e);
        } finally {
            twoCustomers.shutdownNow();
        }
        int remaining = productRepository.findById(lastUnitProductId).orElseThrow().getStock();
        if (lastUnitSuccess.get() != 1) throw new AssertionError("Exactly one customer must buy the last unit; successes=" + lastUnitSuccess.get());
        if (outOfStock.get() != 1) throw new AssertionError("The second customer must receive out-of-stock; failures=" + outOfStock.get());
        if (remaining != 0) throw new AssertionError("Last-unit product stock must be 0, got " + remaining);

        try {
            inventoryService.updateStock(lastUnitProductId, 7);
            if (productRepository.findById(lastUnitProductId).orElseThrow().getStock() != 7)
                throw new AssertionError("Seller stock update should set the requested stock quantity");
            try {
                inventoryService.updateStock(lastUnitProductId, -1);
                throw new AssertionError("Negative stock must be rejected");
            } catch (IllegalArgumentException expected) {
                // Expected validation.
            }
        } catch (ecommerce.exceptions.ProductNotFoundException e) {
            throw new AssertionError("Product unexpectedly missing during stock update", e);
        }
    }
}
