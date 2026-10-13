package ecommerce.concurrency;

import ecommerce.exceptions.InsufficientStockException;
import ecommerce.exceptions.ProductNotFoundException;

public interface InventoryService {
    boolean hasStock(int productId, int quantity);
    void decreaseStock(int productId, int quantity)
            throws InsufficientStockException, ProductNotFoundException;
    void restoreStock(int productId, int quantity)
            throws ProductNotFoundException;

    /** Set the available stock while holding the same per-product lock used by checkout. */
    void updateStock(int productId, int newStock) throws ProductNotFoundException;
}
