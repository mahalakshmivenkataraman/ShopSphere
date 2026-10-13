package ecommerce.exceptions;

public class ProductNotFoundException extends ShopSphereException {
    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(String message) {
        super(message);
    }
}
