package ecommerce.concurrency;

import ecommerce.exceptions.InsufficientStockException;
import ecommerce.exceptions.ProductNotFoundException;
import ecommerce.persistence.FileManager;
import ecommerce.products.Product;
import ecommerce.repository.ProductRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;
    private final FileManager fileManager;
    private final Map<Integer, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    public InventoryServiceImpl(ProductRepository productRepository, FileManager fileManager) {
        this.productRepository = productRepository;
        this.fileManager = fileManager;
    }

    private ReentrantLock getLockForProduct(int productId) {
        return productLocks.computeIfAbsent(productId, k -> new ReentrantLock());
    }

    @Override
    public boolean hasStock(int productId, int quantity) {
        if (quantity <= 0) return false;
        ReentrantLock lock = getLockForProduct(productId);
        lock.lock();
        try {
            return productRepository.findById(productId)
                    .map(p -> p.isActive() && p.getStock() >= quantity)
                    .orElse(false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void decreaseStock(int productId, int quantity)
            throws InsufficientStockException, ProductNotFoundException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        ReentrantLock lock = getLockForProduct(productId);
        lock.lock();
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product ID " + productId + " not found"));

            if (!product.isActive()) {
                throw new ProductNotFoundException("Product ID " + productId + " is inactive");
            }

            if (product.getStock() < quantity) {
                throw new InsufficientStockException("Stock insufficient for '" + product.getName() +
                        "'. Required: " + quantity + ", Available: " + product.getStock());
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            persistProductsSafely();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateStock(int productId, int newStock) throws ProductNotFoundException {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        ReentrantLock lock = getLockForProduct(productId);
        lock.lock();
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product ID " + productId + " not found"));
            product.setStock(newStock);
            productRepository.save(product);
            persistProductsSafely();
        } finally {
            lock.unlock();
        }
    }

    private void persistProductsSafely() {
        if (fileManager != null) {
            // FileManager writes the whole product collection, so serialize writes even when
            // two different product IDs are updated concurrently.
            synchronized (InventoryServiceImpl.class) {
                fileManager.saveProducts(productRepository.findAll());
            }
        }
    }

    @Override
    public void restoreStock(int productId, int quantity)
            throws ProductNotFoundException {
        if (quantity <= 0) {
            return;
        }
        ReentrantLock lock = getLockForProduct(productId);
        lock.lock();
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product ID " + productId + " not found"));

            product.setStock(product.getStock() + quantity);
            productRepository.save(product);

            persistProductsSafely();
        } finally {
            lock.unlock();
        }
    }
}
