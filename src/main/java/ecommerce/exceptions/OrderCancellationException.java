package ecommerce.exceptions;

public class OrderCancellationException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public OrderCancellationException(String message) {
        super(message);
    }
}
