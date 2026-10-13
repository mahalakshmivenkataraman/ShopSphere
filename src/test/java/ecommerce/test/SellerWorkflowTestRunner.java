package ecommerce.test;

import ecommerce.cart.CartItem;
import ecommerce.concurrency.InventoryService;
import ecommerce.concurrency.InventoryServiceImpl;
import ecommerce.orders.Order;
import ecommerce.orders.OrderService;
import ecommerce.orders.OrderStatus;
import ecommerce.persistence.FileManager;
import ecommerce.products.Product;
import ecommerce.repository.OrderRepository;
import ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Dependency-free integration test for the multi-seller dispatch/cancel workflow. */
public final class SellerWorkflowTestRunner {
    private SellerWorkflowTestRunner() { }

    public static void runTests() throws Exception {
        Path dir = Files.createTempDirectory("shopsphere-seller-workflow");
        try {
            FileManager fm = new FileManager(dir.toString());
            ProductRepository products = new ProductRepository();
            OrderRepository orders = new OrderRepository();
            Product first = product(1, "Seller One Product", "S1");
            Product second = product(2, "Seller Two Product", "S2");
            products.save(first); products.save(second);
            InventoryService inventory = new InventoryServiceImpl(products, fm);
            OrderService.setOrderRepository(orders, fm, inventory);

            Order order = new Order("ORD-SELLER-1", "Alex", "Alex", "UPI", 300);
            order.setItems(List.of(new CartItem(first, 1), new CartItem(second, 1)));
            OrderService.createOrder(order);

            require(order.getStatus() == OrderStatus.PROCESSING, "New order should be processing");
            OrderService.dispatchSellerOrder(order.getId(), "S1");
            require(order.getSellerStatus("S1") == OrderStatus.SHIPPED, "Seller 1 should be dispatched");
            require(order.getStatus() == OrderStatus.PROCESSING, "Multi-seller order should remain waiting until all active sellers dispatch");

            OrderService.dispatchSellerOrder(order.getId(), "S2");
            require(order.getStatus() == OrderStatus.DELIVERED, "Order should become completed after all sellers dispatch");
            require(orders.findById(order.getId()).isPresent(), "Order should persist");
        } finally {
            try (var stream = Files.walk(dir)) {
                stream.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        }

        // Separate cancellation check with stock restoration.
        Path dir2 = Files.createTempDirectory("shopsphere-seller-cancel");
        try {
            FileManager fm = new FileManager(dir2.toString());
            ProductRepository products = new ProductRepository();
            OrderRepository orders = new OrderRepository();
            Product product = product(3, "Cancelable Product", "S3");
            products.save(product);
            InventoryService inventory = new InventoryServiceImpl(products, fm);
            inventory.decreaseStock(product.getId(), 2);
            OrderService.setOrderRepository(orders, fm, inventory);
            Order order = new Order("ORD-SELLER-2", "Alex", "Alex", "UPI", 200);
            order.setItems(List.of(new CartItem(product, 2)));
            OrderService.createOrder(order);
            OrderService.cancelSellerOrder(order.getId(), "S3");
            require(order.getStatus() == OrderStatus.CANCELLED, "Seller cancellation should cancel the only seller portion");
            require(product.getStock() == 5, "Cancelled seller stock should be restored");
        } finally {
            try (var stream = Files.walk(dir2)) {
                stream.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        }

        // Regression check: persisted products from older versions may use the seller
        // username (seller1) while the logged-in seller session uses user ID (USR-002).
        Path dir3 = Files.createTempDirectory("shopsphere-seller-identity");
        try {
            FileManager fm = new FileManager(dir3.toString());
            ProductRepository products = new ProductRepository();
            OrderRepository orders = new OrderRepository();
            Product legacy = product(4, "Legacy Seller Product", "seller1");
            legacy.setSellerName("seller1");
            products.save(legacy);
            InventoryService inventory = new InventoryServiceImpl(products, fm);
            OrderService.setOrderRepository(orders, fm, inventory);
            Order order = new Order("ORD-SELLER-LEGACY", "Alex", "Alex", "UPI", 100);
            order.setItems(List.of(new CartItem(legacy, 1)));
            OrderService.createOrder(order);
            require(OrderService.getOrdersForSeller("USR-002", "seller1").size() == 1,
                    "Seller dashboard should find orders using seller ID + username mapping");
            OrderService.dispatchSellerOrder(order.getId(), "USR-002", "seller1");
            require(order.getSellerStatus("seller1") == OrderStatus.SHIPPED,
                    "Seller dispatch should update the legacy username-based seller state");
        } finally {
            try (var stream = Files.walk(dir3)) {
                stream.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        }
    }

    private static Product product(int id, String name, String sellerId) {
        Product p = new Product(id, name, "Test", "Electronics", BigDecimal.valueOf(100), 5, true);
        p.setSellerId(sellerId); p.setSellerName(sellerId.toLowerCase());
        return p;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
