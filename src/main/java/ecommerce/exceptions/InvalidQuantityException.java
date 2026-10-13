package ecommerce.exceptions;

public class InvalidQuantityException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public InvalidQuantityException(String message) {
        super(message);
    }
}
