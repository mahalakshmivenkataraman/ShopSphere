package ecommerce.concurrency;

import ecommerce.orders.Order;

public class NotificationTask implements Runnable {
    private final Order order;

    public NotificationTask(Order order) {
        this.order = order;
    }

    @Override
    public void run() {
        if (order != null) {
            System.out.println("[NotificationTask] Order " + order.getId() +
                    " status updated to: " + order.getStatus() +
                    " for customer: " + order.getCustomerId());
        }
    }
}
