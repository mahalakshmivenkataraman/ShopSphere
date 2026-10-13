package ecommerce.test;

import ecommerce.cart.CartItem;
import ecommerce.orders.Order;
import ecommerce.orders.OrderStatus;
import ecommerce.products.Product;
import ecommerce.persistence.FileManager;
import ecommerce.gui.MainApp;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Dependency-free smoke checks for seller ownership, order line items, and cart persistence. */
public final class ShopSphereCoherenceTestRunner {
    private ShopSphereCoherenceTestRunner() { }

    public static void runTests() throws Exception {
        Product owned = new Product(9001, "Seller Sample", "Test product", "Electronics", new BigDecimal("1499.00"), 8, true);
        owned.setSellerId("seller1");
        owned.setSellerName("seller1");
        CartItem orderLine = new CartItem(owned, 2);
        Order order = new Order("ORD-TEST-1", "Alex", "Alex", "UPI", 2998.00);
        order.setItems(Collections.singletonList(orderLine));
        require(order.getItems().size() == 1, "Order should retain its product line item");
        require("seller1".equals(order.getItems().get(0).getProduct().getSellerId()), "Order line item should retain seller ownership");
        Path orderDir = Files.createTempDirectory("shopsphere-order-test");
        try {
            FileManager fileManager = new FileManager(orderDir.toString());
            fileManager.saveOrders(Collections.singletonList(order));
            List<Order> reloadedOrders = fileManager.loadOrders();
            require(reloadedOrders.size() == 1, "Order should persist across application restarts");
            require("ORD-TEST-1".equals(reloadedOrders.get(0).getOrderId()), "Persisted order ID should remain stable after relogin");
        } finally {
            try (java.util.stream.Stream<Path> files = Files.walk(orderDir)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> { try { Files.deleteIfExists(path); } catch (IOException ignored) { } });
            }
        }

        Map<String, List<MainApp.CartItem>> carts = new LinkedHashMap<>();
        carts.put("Alex", new ArrayList<>(Collections.singletonList(new MainApp.CartItem(owned, 2))));
        Path temp = Files.createTempFile("shopsphere-cart-test", ".dat");
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))) { out.writeObject(carts); }
            Object restored;
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(temp)))) { restored = in.readObject(); }
            require(restored instanceof Map<?, ?>, "Saved carts should deserialize as a map");
            Map<?, ?> restoredMap = (Map<?, ?>) restored;
            List<?> alexCart = (List<?>) restoredMap.get("Alex");
            require(alexCart != null && alexCart.size() == 1, "Alex's cart should persist across sessions");
            MainApp.CartItem item = (MainApp.CartItem) alexCart.get(0);
            require(item.getQuantity() == 2, "Cart quantity should persist");
            require("Seller Sample".equals(item.getProduct().getName()), "Cart product should persist");
        } finally { Files.deleteIfExists(temp); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
