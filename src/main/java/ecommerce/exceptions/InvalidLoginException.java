package ecommerce.exceptions;

public class InvalidLoginException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public InvalidLoginException(String message) {
        super(message);
    }
}
