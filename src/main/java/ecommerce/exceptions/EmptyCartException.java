package ecommerce.exceptions;

public class EmptyCartException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public EmptyCartException(String message) {
        super(message);
    }
}
