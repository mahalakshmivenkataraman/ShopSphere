package ecommerce.orders;

import ecommerce.concurrency.InventoryService;
import ecommerce.exceptions.OrderCancellationException;
import ecommerce.exceptions.OrderNotFoundException;
import ecommerce.exceptions.ProductNotFoundException;
import ecommerce.persistence.FileManager;
import ecommerce.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Business operations for customer and seller order workflows. */
public class OrderService {
    private static OrderRepository orderRepository;
    private static FileManager fileManager;
    private static InventoryService inventoryService;
    private static final List<Order> fallbackOrderDatabase = new ArrayList<>();

    public static void setOrderRepository(OrderRepository repo, FileManager fm, InventoryService invService) {
        orderRepository = repo; fileManager = fm; inventoryService = invService;
    }

    public static void createOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        order.initializeSellerStatuses();
        order.recalculateOverallStatus();
        if (orderRepository != null) {
            orderRepository.save(order);
            persist();
        } else fallbackOrderDatabase.add(order);
    }

    public static List<Order> getOrdersByUser(String userId) {
        if (orderRepository != null) {
            List<Order> list = orderRepository.findByCustomerId(userId);
            if (!list.isEmpty()) return list;
            return orderRepository.findAll().stream().filter(o ->
                    userId != null && (userId.equalsIgnoreCase(o.getUserId()) || userId.equalsIgnoreCase(o.getCustomerName())))
                    .collect(Collectors.toList());
        }
        return fallbackOrderDatabase.stream().filter(o -> userId != null && userId.equalsIgnoreCase(o.getUserId())).collect(Collectors.toList());
    }

    public static List<Order> getOrdersForSeller(String sellerId) {
        return getOrdersForSeller(sellerId, resolveSellerUsername(sellerId));
    }

    /** Robust seller matching for orders created by older ShopSphere versions.
     * Older saved products may contain the seller username instead of the seller user ID,
     * so the seller dashboard accepts either representation.
     */
    public static List<Order> getOrdersForSeller(String sellerId, String sellerUsername) {
        if (sellerId == null && (sellerUsername == null || sellerUsername.isBlank())) return new ArrayList<>();
        return getAllOrders().stream().filter(o -> orderBelongsToSeller(o, sellerId, sellerUsername)).collect(Collectors.toList());
    }

    private static boolean orderBelongsToSeller(Order order, String sellerId, String sellerUsername) {
        if (order == null) return false;
        order.initializeSellerStatuses();
        if (order.containsSeller(sellerId) || order.containsSeller(sellerUsername)) return true;
        if (order.getItems() == null) return false;
        for (var item : order.getItems()) {
            if (item == null || item.getProduct() == null) continue;
            var product = item.getProduct();
            if (sameSeller(product.getSellerId(), sellerId, sellerUsername) ||
                sameSeller(product.getSellerName(), sellerId, sellerUsername)) return true;
        }
        return false;
    }

    private static boolean sameSeller(String value, String sellerId, String sellerUsername) {
        if (value == null || value.isBlank()) return false;
        return value.equalsIgnoreCase(sellerId == null ? "" : sellerId) ||
               value.equalsIgnoreCase(sellerUsername == null ? "" : sellerUsername);
    }

    private static String resolveSellerUsername(String sellerId) {
        // Kept as a compatibility fallback for older callers. The Swing UI passes
        // the real username explicitly to the overloaded methods.
        return sellerId;
    }

    private static String resolveSellerKey(Order order, String sellerId, String sellerUsername) {
        order.initializeSellerStatuses();
        for (String key : new ArrayList<>(order.getSellerStatuses().keySet())) {
            if (sameSeller(key, sellerId, sellerUsername)) return key;
        }
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                if (item == null || item.getProduct() == null) continue;
                var product = item.getProduct();
                if (sameSeller(product.getSellerId(), sellerId, sellerUsername)) return product.getSellerId();
                if (sameSeller(product.getSellerName(), sellerId, sellerUsername)) return product.getSellerId();
            }
        }
        return null;
    }

    public static List<Order> getAllOrders() {
        return orderRepository != null ? orderRepository.findAll() : new ArrayList<>(fallbackOrderDatabase);
    }

    public static void dispatchSellerOrder(String orderId, String sellerId) throws OrderNotFoundException, OrderCancellationException {
        dispatchSellerOrder(orderId, sellerId, resolveSellerUsername(sellerId));
    }

    public static void dispatchSellerOrder(String orderId, String sellerId, String sellerUsername) throws OrderNotFoundException, OrderCancellationException {
        Order order = findOrder(orderId);
        String sellerKey = resolveSellerKey(order, sellerId, sellerUsername);
        if (sellerKey == null) throw new OrderCancellationException("This order does not contain products from your store.");
        OrderStatus current = order.getSellerStatus(sellerKey);
        if (current == OrderStatus.CANCELLED) throw new OrderCancellationException("Your portion of this order is already cancelled.");
        if (current == OrderStatus.SHIPPED || current == OrderStatus.DELIVERED) throw new OrderCancellationException("Your portion of this order is already dispatched.");
        order.setSellerStatus(sellerKey, OrderStatus.SHIPPED);
        order.recalculateOverallStatus();
        persistOrder(order);
    }

    public static void cancelSellerOrder(String orderId, String sellerId) throws OrderNotFoundException, OrderCancellationException {
        cancelSellerOrder(orderId, sellerId, resolveSellerUsername(sellerId));
    }

    public static void cancelSellerOrder(String orderId, String sellerId, String sellerUsername) throws OrderNotFoundException, OrderCancellationException {
        Order order = findOrder(orderId);
        String sellerKey = resolveSellerKey(order, sellerId, sellerUsername);
        if (sellerKey == null) throw new OrderCancellationException("This order does not contain products from your store.");
        OrderStatus current = order.getSellerStatus(sellerKey);
        if (current == OrderStatus.CANCELLED) throw new OrderCancellationException("Your portion is already cancelled.");
        if (current == OrderStatus.SHIPPED || current == OrderStatus.DELIVERED) throw new OrderCancellationException("A dispatched portion cannot be cancelled.");

        if (inventoryService != null && order.getItems() != null) {
            for (var item : order.getItems()) {
                if (item.getProduct() != null && sellerId.equalsIgnoreCase(item.getProduct().getSellerId())) {
                    try { inventoryService.restoreStock(item.getProductId(), item.getQuantity()); }
                    catch (ProductNotFoundException e) { System.err.println("[OrderService] Stock restore failed: " + e.getMessage()); }
                }
            }
        }
        order.setSellerStatus(sellerKey, OrderStatus.CANCELLED);
        order.recalculateOverallStatus();
        persistOrder(order);
    }

    public static void cancelOrder(String orderId) throws OrderNotFoundException, OrderCancellationException {
        Order target = findOrder(orderId);
        if (target.getStatus() == OrderStatus.DELIVERED || target.getStatus() == OrderStatus.CANCELLED)
            throw new OrderCancellationException("Order #" + orderId + " cannot be cancelled in status " + target.getStatus() + ".");
        target.initializeSellerStatuses();
        for (String sellerId : new ArrayList<>(target.getSellerStatuses().keySet())) {
            if (target.getSellerStatus(sellerId) != OrderStatus.SHIPPED && target.getSellerStatus(sellerId) != OrderStatus.DELIVERED) {
                restoreSellerStock(target, sellerId);
                target.setSellerStatus(sellerId, OrderStatus.CANCELLED);
            }
        }
        target.recalculateOverallStatus();
        persistOrder(target);
    }

    private static void restoreSellerStock(Order order, String sellerId) {
        if (inventoryService == null || order.getItems() == null) return;
        for (var item : order.getItems()) {
            if (item.getProduct() != null && sellerId.equalsIgnoreCase(item.getProduct().getSellerId())) {
                try { inventoryService.restoreStock(item.getProductId(), item.getQuantity()); }
                catch (ProductNotFoundException e) { System.err.println("[OrderService] Unable to restore stock: " + e.getMessage()); }
            }
        }
    }

    private static Order findOrder(String orderId) throws OrderNotFoundException {
        Optional<Order> opt = orderRepository != null ? orderRepository.findById(orderId) : Optional.empty();
        if (opt.isPresent()) return opt.get();
        Order fallback = fallbackOrderDatabase.stream().filter(o -> o.getOrderId().equalsIgnoreCase(orderId)).findFirst().orElse(null);
        if (fallback == null) throw new OrderNotFoundException("Order with ID " + orderId + " was not found.");
        return fallback;
    }

    private static void persistOrder(Order order) {
        if (orderRepository != null) { orderRepository.save(order); persist(); }
    }

    private static void persist() {
        if (fileManager != null && orderRepository != null) fileManager.saveOrders(orderRepository.findAll());
    }
}
