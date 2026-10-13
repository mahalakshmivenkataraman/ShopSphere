package ecommerce.exceptions;

public class InvalidCouponException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public InvalidCouponException(String message) {
        super(message);
    }
}
