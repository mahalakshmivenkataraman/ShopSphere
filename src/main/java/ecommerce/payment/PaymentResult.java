package ecommerce.payment;

import java.io.Serializable;

public class PaymentResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String transactionId;

    public PaymentResult() {
    }

    public PaymentResult(boolean success, String message, String transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
