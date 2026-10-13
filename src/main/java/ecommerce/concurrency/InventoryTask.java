package ecommerce.concurrency;

import ecommerce.cart.CartItem;
import ecommerce.exceptions.InsufficientStockException;
import ecommerce.exceptions.ProductNotFoundException;

import java.util.List;

public class InventoryTask implements Runnable {
    private final InventoryService inventoryService;
    private final List<CartItem> items;
    private final boolean isCommit; // true for decrease, false for restore

    public InventoryTask(InventoryService inventoryService, List<CartItem> items, boolean isCommit) {
        this.inventoryService = inventoryService;
        this.items = items;
        this.isCommit = isCommit;
    }

    @Override
    public void run() {
        if (items == null || inventoryService == null) return;
        for (CartItem item : items) {
            try {
                if (isCommit) {
                    inventoryService.decreaseStock(item.getProductId(), item.getQuantity());
                } else {
                    inventoryService.restoreStock(item.getProductId(), item.getQuantity());
                }
            } catch (InsufficientStockException | ProductNotFoundException e) {
                System.err.println("[InventoryTask] Error: " + e.getMessage());
            }
        }
    }
}
