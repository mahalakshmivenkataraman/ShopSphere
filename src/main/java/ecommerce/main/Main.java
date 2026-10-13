package ecommerce.main;

import ecommerce.gui.LoginFrame;
import ecommerce.persistence.FileManager;
import ecommerce.repository.OrderRepository;
import ecommerce.repository.ProductRepository;
import ecommerce.repository.UserRepository;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Application entry point. Use --demo to print a short storage bootstrap summary. */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            System.out.println("ShopSphere storage/concurrency infrastructure");
            System.out.println("Users: " + AppData.USER_REPOSITORY.findAll().size());
            System.out.println("Products: " + AppData.PRODUCT_REPOSITORY.findAll().size());
            System.out.println("Orders: " + AppData.ORDER_REPOSITORY.findAll().size());
            System.out.println("Data folder: " + AppData.FILE_MANAGER.getDataDir());
            ecommerce.util.OopConceptShowcase.runDemo();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new LoginFrame().setVisible(true);
        });
    }
}
