package ecommerce.delivery;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Delivery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String trackingId;
    private String orderId;
    private String address;
    private String status;
    private LocalDateTime estimatedDelivery;

    public Delivery() {
        this.status = "ORDER_PLACED";
        this.estimatedDelivery = LocalDateTime.now().plusDays(3);
    }

    public Delivery(String trackingId, String orderId, String address) {
        this.trackingId = trackingId;
        this.orderId = orderId;
        this.address = address;
        this.status = "IN_TRANSIT";
        this.estimatedDelivery = LocalDateTime.now().plusDays(3);
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }
}
