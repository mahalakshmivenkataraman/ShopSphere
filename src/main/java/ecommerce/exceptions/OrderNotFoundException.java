package ecommerce.exceptions;

public class OrderNotFoundException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(String message) {
        super(message);
    }
}
