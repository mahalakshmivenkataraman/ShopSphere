package ecommerce.exceptions;

public class OrderStateException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public OrderStateException(String message) {
        super(message);
    }
}
