package ecommerce.orders;

import ecommerce.cart.CartItem;
import ecommerce.delivery.DeliveryService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String customerName;
    private String customerId;
    private String paymentMethod;
    private List<CartItem> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    /** Per-seller fulfillment state. This keeps multi-seller orders coherent. */
    private Map<String, OrderStatus> sellerStatuses;

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.paymentMethod = "Credit Card / UPI";
        this.sellerStatuses = new LinkedHashMap<>();
    }

    public Order(String id, String customerId, List<CartItem> items, BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.tax = tax != null ? tax : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.status = status != null ? status : OrderStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
        this.paymentMethod = "Credit Card / UPI";
        this.sellerStatuses = new LinkedHashMap<>();
        initializeSellerStatuses();
    }

    // Person 1 & 2 Swing GUI constructor
    public Order(String orderId, String customerName, String userId, String paymentMethod, double totalPaid) {
        this.id = orderId;
        this.customerName = customerName;
        this.customerId = userId;
        this.paymentMethod = paymentMethod;
        this.total = BigDecimal.valueOf(totalPaid);
        this.subtotal = BigDecimal.valueOf(totalPaid);
        this.discount = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PROCESSING;
        this.createdAt = LocalDateTime.now();
        this.sellerStatuses = new LinkedHashMap<>();
    }

    // Identifiers & Aliases
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return id; }

    public String getCustomerName() { return customerName != null ? customerName : customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getUserId() { return customerId; }

    public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "Credit Card / UPI"; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        initializeSellerStatuses();
        recalculateOverallStatus();
    }

    public Map<String, OrderStatus> getSellerStatuses() {
        if (sellerStatuses == null) sellerStatuses = new LinkedHashMap<>();
        return sellerStatuses;
    }

    public void setSellerStatuses(Map<String, OrderStatus> statuses) {
        this.sellerStatuses = statuses != null ? new LinkedHashMap<>(statuses) : new LinkedHashMap<>();
        recalculateOverallStatus();
    }

    /** Creates a pending fulfillment state for every seller represented in this order. */
    public void initializeSellerStatuses() {
        if (sellerStatuses == null) sellerStatuses = new LinkedHashMap<>();
        if (items == null) return;
        for (CartItem item : items) {
            if (item == null || item.getProduct() == null) continue;
            String sellerId = item.getProduct().getSellerId();
            if (sellerId != null && !sellerId.isBlank()) sellerStatuses.putIfAbsent(sellerId, OrderStatus.PROCESSING);
        }
    }

    public OrderStatus getSellerStatus(String sellerId) {
        initializeSellerStatuses();
        return sellerStatuses.getOrDefault(sellerId, OrderStatus.PROCESSING);
    }

    public void setSellerStatus(String sellerId, OrderStatus status) {
        if (sellerId == null || sellerId.isBlank() || status == null) return;
        initializeSellerStatuses();
        sellerStatuses.put(sellerId, status);
        recalculateOverallStatus();
    }

    public boolean containsSeller(String sellerId) {
        initializeSellerStatuses();
        return sellerStatuses.containsKey(sellerId);
    }

    /** Overall customer-facing status derived from all seller fulfillment states. */
    public void recalculateOverallStatus() {
        initializeSellerStatuses();
        if (sellerStatuses.isEmpty()) return;
        boolean hasActive = false, allFinished = true;
        for (OrderStatus status : sellerStatuses.values()) {
            if (status != OrderStatus.CANCELLED) {
                hasActive = true;
                if (status != OrderStatus.SHIPPED && status != OrderStatus.DELIVERED) allFinished = false;
            }
        }
        if (!hasActive) { status = OrderStatus.CANCELLED; return; }
        if (allFinished) status = OrderStatus.DELIVERED;
        else status = OrderStatus.PROCESSING;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public double getTotalPaid() { return total != null ? total.doubleValue() : 0.0; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public void setStatus(String statusStr) {
        if (statusStr == null) return;
        try {
            this.status = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            if ("CANCELLED".equalsIgnoreCase(statusStr)) this.status = OrderStatus.CANCELLED;
            else if ("PROCESSING".equalsIgnoreCase(statusStr)) this.status = OrderStatus.PROCESSING;
            else this.status = OrderStatus.PENDING_PAYMENT;
        }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Date getOrderDate() {
        return createdAt != null ? java.sql.Timestamp.valueOf(createdAt) : new Date();
    }

    public String getTrackingStatus() {
        return DeliveryService.getDeliveryStatus(status != null ? status.name() : "PROCESSING");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", itemCount=" + (items != null ? items.size() : 0) +
                ", total=" + total +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
