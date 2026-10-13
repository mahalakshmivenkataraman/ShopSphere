package ecommerce.exceptions;

public class InsufficientStockException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
