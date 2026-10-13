package ecommerce.delivery;

public class DeliveryService {

    public static String getDeliveryStatus(String orderStatus) {
        if (orderStatus == null) {
            return "Status Unknown";
        }

        switch (orderStatus.toUpperCase()) {
            case "PROCESSING":
            case "CONFIRMED":
            case "PENDING_PAYMENT":
                return "Package is being prepared at warehouse.";
            case "SHIPPED":
                return "In Transit — On the way to delivery hub.";
            case "DELIVERED":
                return "Delivered successfully.";
            case "CANCELLED":
                return "Order cancelled — No shipment active.";
            case "PAYMENT_FAILED":
                return "Payment Failed — Order put on hold.";
            default:
                return "Tracking details pending.";
        }
    }
}
