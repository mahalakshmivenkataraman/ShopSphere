package ecommerce.concurrency;

import ecommerce.orders.Order;
import ecommerce.payment.PaymentResult;
import ecommerce.payment.PaymentType;

import java.util.UUID;
import java.util.concurrent.Callable;

public class PaymentTask implements Callable<PaymentResult> {
    private final Order order;
    private final PaymentType paymentType;

    public PaymentTask(Order order, PaymentType paymentType) {
        this.order = order;
        this.paymentType = paymentType;
    }

    @Override
    public PaymentResult call() throws Exception {
        // Simulate network processing latency
        Thread.sleep(100);
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, "Simulated payment successful via " + paymentType, txnId);
    }
}
