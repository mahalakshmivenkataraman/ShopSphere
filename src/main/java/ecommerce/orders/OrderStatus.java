package ecommerce.orders;

public enum OrderStatus {
    PENDING_PAYMENT,
    PROCESSING,
    CONFIRMED,
    CANCELLED,
    SHIPPED,
    DELIVERED,
    PAYMENT_FAILED
}
