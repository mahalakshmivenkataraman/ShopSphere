package ecommerce.main;

import ecommerce.concurrency.InventoryService;
import ecommerce.concurrency.InventoryServiceImpl;
import ecommerce.orders.OrderService;
import ecommerce.persistence.FileManager;
import ecommerce.repository.OrderRepository;
import ecommerce.repository.ProductRepository;
import ecommerce.repository.UserRepository;
import ecommerce.util.SeedDataGenerator;

/** Shared application data layer used by the Swing UI and service classes. */
public final class AppData {
    public static final FileManager FILE_MANAGER = new FileManager("data");
    public static final UserRepository USER_REPOSITORY = new UserRepository();
    public static final ProductRepository PRODUCT_REPOSITORY = new ProductRepository();
    public static final OrderRepository ORDER_REPOSITORY = new OrderRepository();
    public static final InventoryService INVENTORY_SERVICE = new InventoryServiceImpl(PRODUCT_REPOSITORY, FILE_MANAGER);

    static {
        FILE_MANAGER.loadAll(PRODUCT_REPOSITORY, USER_REPOSITORY, ORDER_REPOSITORY);
        SeedDataGenerator.initializeSeedData(USER_REPOSITORY, PRODUCT_REPOSITORY, FILE_MANAGER);
        // Rebind persisted order lines to the current product repository so seller ownership
        // and updated product details survive logout/login and application restarts.
        for (var order : ORDER_REPOSITORY.findAll()) {
            if (order.getItems() != null) {
                for (var item : order.getItems()) {
                    PRODUCT_REPOSITORY.findById(item.getProductId()).ifPresent(item::setProduct);
                }
            }
            order.initializeSellerStatuses();
            order.recalculateOverallStatus();
        }
        FILE_MANAGER.saveOrders(ORDER_REPOSITORY.findAll());
        OrderService.setOrderRepository(ORDER_REPOSITORY, FILE_MANAGER, INVENTORY_SERVICE);
    }

    private AppData() { }
}
