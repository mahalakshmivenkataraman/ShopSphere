package ecommerce.exceptions;

public class ShopSphereException extends Exception {
    private static final long serialVersionUID = 1L;

    public ShopSphereException(String message) {
        super(message);
    }

    public ShopSphereException(String message, Throwable cause) {
        super(message, cause);
    }
}
