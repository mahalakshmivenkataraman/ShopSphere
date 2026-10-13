package ecommerce.exceptions;

public class UserNotFoundException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }
}
