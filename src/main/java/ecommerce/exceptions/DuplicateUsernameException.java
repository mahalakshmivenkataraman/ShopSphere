package ecommerce.exceptions;

public class DuplicateUsernameException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
