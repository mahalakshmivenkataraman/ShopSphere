package ecommerce.gui;

import ecommerce.coupon.CouponService;
import ecommerce.exceptions.InvalidCouponException;
import ecommerce.orders.Order;
import ecommerce.orders.OrderService;
import ecommerce.products.Product;
import ecommerce.main.AppData;
import ecommerce.payment.PaymentType;
import ecommerce.payment.PaymentResult;
import ecommerce.concurrency.PaymentTask;
import ecommerce.exceptions.InsufficientStockException;
import ecommerce.exceptions.ProductNotFoundException;
import ecommerce.exceptions.OrderCancellationException;
import ecommerce.exceptions.OrderNotFoundException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainApp extends JFrame {

    private String username;
    private String userId;
    private String role;

    // Theme Colors (Matching Dark Theme)
    private Color BG_DARK = new Color(15, 23, 42);
    private Color PANEL_DARK = new Color(30, 41, 59);
    private Color ACCENT_PURPLE = new Color(139, 92, 246);
    private Color TEXT_LIGHT = new Color(248, 250, 252);
    private Color TEXT_MUTED = new Color(148, 163, 184);
    private Color BORDER_COLOR = new Color(51, 65, 85);
    private Color SUCCESS_GREEN = new Color(34, 197, 94);
    private Color DANGER_RED = new Color(239, 68, 68);
    private boolean darkTheme = true;

    // Global Product Database (9 Categories, 12 products each)
    public static final List<Product> globalProductList = new ArrayList<>();
    private static final Map<String, List<CartItem>> CARTS_BY_USER = loadSavedCarts();
    private static final java.util.List<MainApp> OPEN_APPS = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<CartItem> cart;

    // Table Models for dynamic refreshing across tabs
    private DefaultTableModel cartTableModel;
    private DefaultTableModel ordersTableModel;
    private DefaultTableModel adminCartsTableModel;
    private DefaultTableModel sellerProductsTableModel;
    private DefaultTableModel completedOrdersTableModel;

    // 9 Categories
    public static final String[] CATEGORIES = {
        "Electronics", "Furniture", "Clothing & Fashion", 
        "Books & Stationery", "Sports & Outdoors", "Home & Kitchen", 
        "Beauty & Personal Care", "Toys & Games", "Groceries & Health"
    };

    static {
        initializeProducts();
    }

    public static class CartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public MainApp(String username, String userId, String role) {
        this.username = username;
        this.userId = userId != null ? userId : username;
        this.role = role;
        synchronized (CARTS_BY_USER) {
            this.cart = CARTS_BY_USER.computeIfAbsent(this.userId, k -> new ArrayList<>());
            for (CartItem item : this.cart) {
                AppData.PRODUCT_REPOSITORY.findById(item.getProduct().getId()).ifPresent(item::setProduct);
            }
            saveAllCarts();
        }

        setTitle("ShopSphere - Dashboard (" + role + " - " + username + ")");
        setSize(1250, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        initUI();
        OPEN_APPS.add(this);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { OPEN_APPS.remove(MainApp.this); }
        });
    }

    public MainApp(String username, String role) {
        this(username, username, role);
    }

    private void initUI() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        // TOP NAVIGATION BAR
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(PANEL_DARK);
        topNav.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Enhanced Logo Section with Custom Icon Badge & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JPanel logoIconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(139, 92, 246), getWidth(), getHeight(), new Color(88, 28, 135));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        logoIconBadge.setPreferredSize(new Dimension(36, 36));
        logoIconBadge.setLayout(new GridBagLayout());
        JLabel bagSymbol = new JLabel("🛍");
        bagSymbol.setFont(new Font("SansSerif", Font.PLAIN, 16));
        logoIconBadge.add(bagSymbol);

        JLabel brandLabel = new JLabel("ShopSphere (" + role + ")");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        brandLabel.setForeground(TEXT_LIGHT);

        brandPanel.add(logoIconBadge);
        brandPanel.add(brandLabel);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("Welcome, " + username);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userLabel.setForeground(TEXT_MUTED);

        JButton themeBtn = createStyledButton(darkTheme ? "Light Theme" : "Dark Theme", ACCENT_PURPLE, new Dimension(115, 32));
        themeBtn.addActionListener(e -> {
            darkTheme = !darkTheme;
            if (darkTheme) {
                BG_DARK = new Color(15, 23, 42); PANEL_DARK = new Color(30, 41, 59);
                ACCENT_PURPLE = new Color(139, 92, 246); TEXT_LIGHT = new Color(248, 250, 252);
                TEXT_MUTED = new Color(148, 163, 184); BORDER_COLOR = new Color(51, 65, 85);
                SUCCESS_GREEN = new Color(34, 197, 94); DANGER_RED = new Color(239, 68, 68);
            } else {
                BG_DARK = new Color(248, 250, 252); PANEL_DARK = Color.WHITE;
                ACCENT_PURPLE = new Color(109, 40, 217); TEXT_LIGHT = new Color(30, 41, 59);
                TEXT_MUTED = new Color(71, 85, 105); BORDER_COLOR = new Color(203, 213, 225);
                SUCCESS_GREEN = new Color(22, 163, 74); DANGER_RED = new Color(220, 38, 38);
            }
            getContentPane().setBackground(BG_DARK);
            initUI(); revalidate(); repaint();
        });

        JButton logoutBtn = createStyledButton("Logout", DANGER_RED, new Dimension(90, 32));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            OPEN_APPS.remove(this);
            dispose();
        });

        userPanel.add(userLabel);
        userPanel.add(themeBtn);
        userPanel.add(logoutBtn);

        topNav.add(brandPanel, BorderLayout.WEST);
        topNav.add(userPanel, BorderLayout.EAST);
        add(topNav, BorderLayout.NORTH);

        // MAIN CONTENT TABS BASED ON ROLE
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(TEXT_LIGHT);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(PANEL_DARK);
                } else {
                    g2.setColor(BG_DARK);
                }
                g2.fillRect(x, y, w, h);
                g2.dispose();
            }
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // Do nothing to eliminate focus rectangle artifacts
            }
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_COLOR);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        });

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            tabbedPane.addTab("Catalog & Shopping", createCatalogPanel());
            tabbedPane.addTab("My Cart", createCartPanel());
            tabbedPane.addTab("Waiting Orders", createCustomerOrdersPanel(false));
            tabbedPane.addTab("Completed Orders", createCustomerOrdersPanel(true));
        } else if ("SELLER".equalsIgnoreCase(role)) {
            tabbedPane.addTab("Manage Inventory & My Products", createSellerInventoryPanel());
            tabbedPane.addTab("Orders for My Products", createSellerOrdersPanel());
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            tabbedPane.addTab("All Products Catalog", createCatalogPanel());
            tabbedPane.addTab("My Cart", createCartPanel());
            tabbedPane.addTab("All Customer Carts", createAdminCartsPanel());
            tabbedPane.addTab("System Inventory Control", createSellerInventoryPanel());
            tabbedPane.addTab("All System Orders", createOrdersPanel());
        }

        tabbedPane.addChangeListener(e -> {
            refreshCartTable();
            refreshAdminCartsTable();
            refreshSellerProductsTable();
            refreshOrdersTable();
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==================== CATALOG & SHOPPING PANEL ====================
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        JPanel leftSidebar = new JPanel();
        leftSidebar.setLayout(new BoxLayout(leftSidebar, BoxLayout.Y_AXIS));
        leftSidebar.setBackground(PANEL_DARK);
        leftSidebar.setBorder(new EmptyBorder(15, 15, 15, 15));
        leftSidebar.setPreferredSize(new Dimension(220, 0));

        JLabel sidebarTitle = new JLabel("Categories");
        sidebarTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        sidebarTitle.setForeground(ACCENT_PURPLE);
        sidebarTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftSidebar.add(sidebarTitle);
        leftSidebar.add(Box.createVerticalStrut(15));

        JPanel rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.setBackground(BG_DARK);
        rightContentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel activeCategoryLabel = new JLabel("Showing: All Categories");
        activeCategoryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        activeCategoryLabel.setForeground(TEXT_LIGHT);

        JTextField searchField = new JTextField(18);
        searchField.setBackground(PANEL_DARK);
        searchField.setForeground(TEXT_LIGHT);
        searchField.setCaretColor(TEXT_LIGHT);
        
        JButton searchBtn = createStyledButton("Search", ACCENT_PURPLE, new Dimension(85, 30));

        searchBarPanel.add(activeCategoryLabel);
        searchBarPanel.add(Box.createHorizontalStrut(20));
        searchBarPanel.add(new JLabel("Search:") {{ setForeground(TEXT_LIGHT); }});
        searchBarPanel.add(searchField);
        searchBarPanel.add(searchBtn);

        rightContentPanel.add(searchBarPanel, BorderLayout.NORTH);

        JPanel gridContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridContainer.setBackground(BG_DARK);

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(null);
        rightContentPanel.add(scrollPane, BorderLayout.CENTER);

        final String[] selectedCategory = {"All Categories"};

        Runnable loadProducts = () -> {
            gridContainer.removeAll();
            String query = searchField.getText().trim().toLowerCase();

            List<Product> filtered = globalProductList.stream().filter(p -> {
                boolean matchCat = selectedCategory[0].equals("All Categories") || p.getCategory().equalsIgnoreCase(selectedCategory[0]);
                boolean matchQuery = query.isEmpty() || p.getName().toLowerCase().contains(query) || p.getDescription().toLowerCase().contains(query);
                return matchCat && matchQuery;
            }).collect(Collectors.toList());

            for (Product p : filtered) {
                JPanel card = createProductCard(p);
                gridContainer.add(card);
            }
            gridContainer.revalidate();
            gridContainer.repaint();
        };

        JButton allCatBtn = createCategorySidebarButton("📂 All Categories");
        allCatBtn.addActionListener(e -> {
            selectedCategory[0] = "All Categories";
            activeCategoryLabel.setText("Showing: All Categories");
            loadProducts.run();
        });
        leftSidebar.add(allCatBtn);
        leftSidebar.add(Box.createVerticalStrut(8));

        for (String cat : CATEGORIES) {
            JButton catBtn = createCategorySidebarButton("▪ " + cat);
            catBtn.addActionListener(e -> {
                selectedCategory[0] = cat;
                activeCategoryLabel.setText("Showing: " + cat);
                loadProducts.run();
            });
            leftSidebar.add(catBtn);
            leftSidebar.add(Box.createVerticalStrut(6));
        }

        searchBtn.addActionListener(e -> loadProducts.run());
        searchField.addActionListener(e -> loadProducts.run());

        loadProducts.run();

        panel.add(leftSidebar, BorderLayout.WEST);
        panel.add(rightContentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createCategorySidebarButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(darkTheme ? new Color(30, 41, 59) : new Color(226, 232, 240));
                } else if (getModel().isRollover()) {
                    g2.setColor(darkTheme ? new Color(51, 65, 85) : new Color(241, 245, 249));
                } else {
                    g2.setColor(darkTheme ? new Color(45, 55, 72) : new Color(255, 255, 255));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(TEXT_LIGHT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 32));
        btn.setPreferredSize(new Dimension(190, 32));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(280, 285));

        JLabel imageLabel = new JLabel(loadProductIcon(p, 230, 125));
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(230, 125));
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { showProductDetails(p); }
        });

        JLabel nameLbl = new JLabel(p.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 15)); nameLbl.setForeground(TEXT_LIGHT);
        JLabel catLbl = new JLabel("[" + p.getCategory() + "]  •  Seller: " + (p.getSellerName() == null ? "ShopSphere" : p.getSellerName()));
        catLbl.setFont(new Font("SansSerif", Font.ITALIC, 11)); catLbl.setForeground(ACCENT_PURPLE);
        JLabel priceLbl = new JLabel("Price: " + p.getFormattedRupeePrice());
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 13)); priceLbl.setForeground(SUCCESS_GREEN);
        JButton detailsBtn = createStyledButton("View Details", new Color(71, 85, 105), new Dimension(110, 30));
        detailsBtn.addActionListener(e -> showProductDetails(p));
        JButton addCartBtn = createStyledButton("Add to Cart", ACCENT_PURPLE, new Dimension(120, 30));
        addCartBtn.addActionListener(e -> {
            if (addProductToCart(p)) {
                refreshCartTable(); refreshAdminCartsTable();
                JOptionPane.showMessageDialog(this, p.getName() + " added to cart!", "Cart Update", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); actions.setOpaque(false);
        actions.add(detailsBtn); actions.add(addCartBtn);
        card.add(imageLabel); card.add(Box.createVerticalStrut(5)); card.add(nameLbl);
        card.add(Box.createVerticalStrut(3)); card.add(catLbl); card.add(Box.createVerticalStrut(4));
        card.add(priceLbl); card.add(Box.createVerticalStrut(5)); card.add(actions);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        java.awt.event.MouseAdapter openDetails = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { showProductDetails(p); }
        };
        card.addMouseListener(openDetails); nameLbl.addMouseListener(openDetails); catLbl.addMouseListener(openDetails); priceLbl.addMouseListener(openDetails);
        return card;
    }

    private ImageIcon loadProductIcon(Product product, int width, int height) {
        try {
            java.io.File imageFile = product.getImagePath() == null ? null : new java.io.File(product.getImagePath());
            if (imageFile != null && imageFile.isFile()) {
                Image image = new ImageIcon(imageFile.getAbsolutePath()).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            }
        } catch (Exception ignored) { }
        // A generated, category-colored fallback keeps custom products visible even without an uploaded photo.
        java.awt.image.BufferedImage fallback = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g = fallback.createGraphics();
        float hue = (float) ((Math.abs(product.getId() * 0.61803398875) + (product.getCategory() == null ? 0 : product.getCategory().hashCode() * 0.00001)) % 1.0);
        Color tint = Color.getHSBColor(hue, 0.62f, 0.88f); g.setColor(tint); g.fillRoundRect(0, 0, width, height, 18, 18);
        g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String symbol = categorySymbol(product.getCategory()); g.drawString(symbol, width/2 - 20, height/2 - 2);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String label = product.getName(); if (label.length() > 30) label = label.substring(0, 27) + "...";
        g.drawString(label, 8, height - 12); g.dispose();
        return new ImageIcon(fallback);
    }

    private Color categoryColor(String category) {
        if (category == null) return new Color(100, 116, 139);
        switch (category) {
            case "Electronics": return new Color(37, 99, 235);
            case "Furniture": return new Color(120, 113, 108);
            case "Clothing & Fashion": return new Color(190, 24, 93);
            case "Books & Stationery": return new Color(124, 58, 237);
            case "Sports & Outdoors": return new Color(5, 150, 105);
            case "Home & Kitchen": return new Color(217, 119, 6);
            case "Beauty & Personal Care": return new Color(219, 39, 119);
            case "Toys & Games": return new Color(8, 145, 178);
            default: return new Color(101, 163, 13);
        }
    }

    private String categorySymbol(String category) {
        if (category == null) return "SHOP";
        if (category.contains("Electronics")) return "TECH";
        if (category.contains("Furniture")) return "HOME";
        if (category.contains("Clothing")) return "STYLE";
        if (category.contains("Books")) return "READ";
        if (category.contains("Sports")) return "SPORT";
        if (category.contains("Kitchen")) return "COOK";
        if (category.contains("Beauty")) return "CARE";
        if (category.contains("Toys")) return "PLAY";
        return "FRESH";
    }

    private void showProductDetails(Product product) {
        JDialog dialog = new JDialog(this, "Product Details - " + product.getName(), true);
        dialog.setSize(700, 590); dialog.setLocationRelativeTo(this); dialog.setLayout(new BorderLayout(12, 12));
        JPanel details = new JPanel(new BorderLayout(16, 12)); details.setBackground(PANEL_DARK); details.setBorder(new EmptyBorder(18, 18, 10, 18));
        JLabel image = new JLabel(loadProductIcon(product, 260, 230)); image.setVerticalAlignment(SwingConstants.TOP);
        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setOpaque(false);
        JLabel name = new JLabel(product.getName()); name.setFont(new Font("SansSerif", Font.BOLD, 22)); name.setForeground(TEXT_LIGHT);
        JLabel category = new JLabel("Category: " + product.getCategory()); category.setForeground(ACCENT_PURPLE);
        JLabel seller = new JLabel("Sold by: " + (product.getSellerName() == null ? "ShopSphere" : product.getSellerName())); seller.setForeground(TEXT_LIGHT);
        JLabel price = new JLabel("Price: " + product.getFormattedRupeePrice()); price.setFont(new Font("SansSerif", Font.BOLD, 18)); price.setForeground(SUCCESS_GREEN);
        JLabel stock = new JLabel("Availability: " + product.getStock() + " in stock"); stock.setForeground(TEXT_LIGHT);
        JTextArea description = new JTextArea(product.getDescription()); description.setWrapStyleWord(true); description.setLineWrap(true); description.setEditable(false); description.setOpaque(false); description.setForeground(TEXT_MUTED); description.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.add(name); info.add(Box.createVerticalStrut(12)); info.add(category); info.add(Box.createVerticalStrut(6)); info.add(seller); info.add(Box.createVerticalStrut(8)); info.add(price); info.add(Box.createVerticalStrut(8)); info.add(stock); info.add(Box.createVerticalStrut(15)); info.add(new JLabel("About this product")); info.add(Box.createVerticalStrut(6)); info.add(description);
        details.add(image, BorderLayout.WEST); details.add(info, BorderLayout.CENTER); dialog.add(details, BorderLayout.NORTH);
        JPanel relatedPanel = new JPanel(new BorderLayout(6, 6)); relatedPanel.setBackground(BG_DARK); relatedPanel.setBorder(new EmptyBorder(0, 18, 8, 18));
        JLabel relatedTitle = new JLabel("You may also like"); relatedTitle.setFont(new Font("SansSerif", Font.BOLD, 16)); relatedTitle.setForeground(TEXT_LIGHT); relatedPanel.add(relatedTitle, BorderLayout.NORTH);
        JPanel relatedCards = new JPanel(new GridLayout(1, 3, 10, 5)); relatedCards.setOpaque(false);
        List<Product> related = globalProductList.stream().filter(p -> p.getId() != product.getId() && p.getCategory().equalsIgnoreCase(product.getCategory())).limit(3).collect(Collectors.toList());
        if (related.isEmpty()) related = globalProductList.stream().filter(p -> p.getId() != product.getId()).limit(3).collect(Collectors.toList());
        for (Product item : related) {
            JButton relatedButton = new JButton("<html><center>" + item.getName() + "<br>" + item.getFormattedRupeePrice() + "</center></html>", loadProductIcon(item, 90, 60));
            relatedButton.setHorizontalTextPosition(SwingConstants.CENTER); relatedButton.setVerticalTextPosition(SwingConstants.BOTTOM); relatedButton.setForeground(TEXT_LIGHT); relatedButton.setBackground(PANEL_DARK); relatedButton.addActionListener(e -> { dialog.dispose(); showProductDetails(item); }); relatedCards.add(relatedButton);
        }
        relatedPanel.add(relatedCards, BorderLayout.CENTER); dialog.add(relatedPanel, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setBackground(BG_DARK);
        JButton add = createStyledButton("Add to Cart", ACCENT_PURPLE, new Dimension(130, 34)); add.addActionListener(e -> { if (addProductToCart(product)) { refreshCartTable(); refreshAdminCartsTable(); dialog.dispose(); JOptionPane.showMessageDialog(this, product.getName() + " added to cart."); } });
        JButton close = createStyledButton("Close", DANGER_RED, new Dimension(90, 34)); close.addActionListener(e -> dialog.dispose()); bottom.add(add); bottom.add(close); dialog.add(bottom, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    // ==================== CART & CHECKOUT PANEL ====================
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        cartTableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Category", "Price (INR)", "Qty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(cartTableModel);
        table.setBackground(PANEL_DARK);
        table.setForeground(TEXT_LIGHT);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(PANEL_DARK);
        table.getTableHeader().setForeground(TEXT_LIGHT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(PANEL_DARK);
                c.setForeground(TEXT_LIGHT);
                c.setFont(new Font("SansSerif", Font.BOLD, 12));
                c.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_COLOR));
                return c;
            }
        });

        refreshCartTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(PANEL_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Enhanced Polished Dark-Themed Checkout Footer Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(20, 10));
        bottomPanel.setBackground(PANEL_DARK);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 20, 16, 20)
        ));

        // Left Side Inputs: Coupon Code & Payment Method Selector
        JPanel inputsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        inputsPanel.setOpaque(false);

        JLabel couponLabel = new JLabel("Coupon Code:");
        couponLabel.setForeground(TEXT_LIGHT);
        couponLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JTextField couponField = new JTextField(10);
        couponField.setBackground(BG_DARK);
        couponField.setForeground(TEXT_LIGHT);
        couponField.setCaretColor(TEXT_LIGHT);
        couponField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        couponField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JLabel paymentLabel = new JLabel("Payment Method:");
        paymentLabel.setForeground(TEXT_LIGHT);
        paymentLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        String[] paymentMethods = {"Credit Card / UPI", "PayPal", "Net Banking", "Cash on Delivery"};
        JComboBox<String> paymentCombo = new JComboBox<>(paymentMethods);
        paymentCombo.setBackground(BG_DARK);
        paymentCombo.setForeground(TEXT_LIGHT);
        paymentCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        paymentCombo.setPreferredSize(new Dimension(160, 32));
        paymentCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Custom Dark Renderer for Dropdown Items & Selected View
        paymentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? ACCENT_PURPLE : BG_DARK);
                c.setForeground(TEXT_LIGHT);
                c.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return c;
            }
        });

        inputsPanel.add(couponLabel);
        inputsPanel.add(couponField);
        inputsPanel.add(Box.createHorizontalStrut(10));
        inputsPanel.add(paymentLabel);
        inputsPanel.add(paymentCombo);

        // Right Side Actions: Clear Cart & Checkout Buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        JButton clearBtn = createStyledButton("Clear Cart", DANGER_RED, new Dimension(120, 38));
        clearBtn.addActionListener(e -> {
            cart.clear(); saveAllCarts();
            couponField.setText("");
            refreshCartTable(); refreshAdminCartsTable();
        });

        JButton checkoutBtn = createStyledButton("Proceed to Checkout", SUCCESS_GREEN, new Dimension(180, 38));
        checkoutBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty!", "Checkout Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double totalAmount = cart.stream().mapToDouble(i -> i.getProduct().getPriceAsDouble() * i.getQuantity()).sum();
            String couponCode = couponField.getText().trim();
            try { totalAmount = CouponService.applyCoupon(couponCode, totalAmount); }
            catch (InvalidCouponException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Coupon Error", JOptionPane.ERROR_MESSAGE); return;
            }
            showPaymentReview(totalAmount, couponCode, (String) paymentCombo.getSelectedItem());
        });

        JButton removeItemBtn = createStyledButton("Remove Selected", new Color(217, 119, 6), new Dimension(145, 38));
        removeItemBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) { JOptionPane.showMessageDialog(this, "Select a cart item to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
            int productId = Integer.parseInt(String.valueOf(cartTableModel.getValueAt(selectedRow, 0)));
            cart.removeIf(item -> item.getProduct().getId() == productId);
            saveAllCarts(); refreshCartTable(); refreshAdminCartsTable();
        });
        actionsPanel.add(removeItemBtn);
        actionsPanel.add(clearBtn);
        actionsPanel.add(checkoutBtn);

        bottomPanel.add(inputsPanel, BorderLayout.WEST);
        bottomPanel.add(actionsPanel, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showPaymentReview(double totalAmount, String couponCode, String selectedPayment) {
        JDialog paymentDialog = new JDialog(this, "Review & Pay - ShopSphere", true);
        paymentDialog.setSize(540, 440); paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setLayout(new BorderLayout(12, 12));
        JPanel body = new JPanel(new BorderLayout(10, 10)); body.setBackground(PANEL_DARK); body.setBorder(new EmptyBorder(22, 24, 18, 24));
        JLabel title = new JLabel("Review your order"); title.setFont(new Font("SansSerif", Font.BOLD, 23)); title.setForeground(TEXT_LIGHT);
        body.add(title, BorderLayout.NORTH);
        StringBuilder summary = new StringBuilder("Please confirm your items before continuing to payment.\n\n");
        for (CartItem item : cart) summary.append("• ").append(item.getProduct().getName()).append(" × ").append(item.getQuantity()).append("   ").append(item.getProduct().getFormattedRupeePrice()).append(" each\n");
        summary.append("\nPayment method: ").append(selectedPayment == null ? "Credit Card / UPI" : selectedPayment);
        if (!couponCode.isEmpty()) summary.append("\nCoupon: ").append(couponCode);
        JTextArea summaryArea = new JTextArea(summary.toString()); summaryArea.setEditable(false); summaryArea.setLineWrap(true); summaryArea.setWrapStyleWord(true); summaryArea.setFont(new Font("SansSerif", Font.PLAIN, 14)); summaryArea.setForeground(TEXT_LIGHT); summaryArea.setBackground(PANEL_DARK); summaryArea.setBorder(new EmptyBorder(10, 0, 10, 0));
        body.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        JLabel totalLabel = new JLabel("Amount to pay: " + java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(totalAmount));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20)); totalLabel.setForeground(SUCCESS_GREEN); body.add(totalLabel, BorderLayout.SOUTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); actions.setBackground(BG_DARK);
        JButton back = createStyledButton("Back to Cart", new Color(100, 116, 139), new Dimension(120, 38)); back.addActionListener(e -> paymentDialog.dispose());
        JButton pay = createStyledButton("Pay " + java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(totalAmount), SUCCESS_GREEN, new Dimension(190, 38));
        pay.addActionListener(e -> { paymentDialog.dispose(); processCheckout(totalAmount, couponCode, selectedPayment); });
        actions.add(back); actions.add(pay); paymentDialog.add(body, BorderLayout.CENTER); paymentDialog.add(actions, BorderLayout.SOUTH); paymentDialog.setVisible(true);
    }

    private void processCheckout(double totalAmount, String couponCode, String selectedPayment) {
        String orderId = "ORD-" + System.currentTimeMillis();
        java.util.List<ecommerce.cart.CartItem> orderItems = new java.util.ArrayList<>();
        java.util.List<ecommerce.cart.CartItem> stockAdjusted = new java.util.ArrayList<>();
        try {
            for (CartItem item : cart) {
                ecommerce.cart.CartItem savedItem = new ecommerce.cart.CartItem(item.getProduct(), item.getQuantity());
                AppData.INVENTORY_SERVICE.decreaseStock(item.getProduct().getId(), item.getQuantity());
                stockAdjusted.add(savedItem); orderItems.add(savedItem);
            }
        } catch (InsufficientStockException | ProductNotFoundException ex) {
            restoreReservedStock(stockAdjusted); JOptionPane.showMessageDialog(this, "Checkout could not be completed: " + ex.getMessage(), "Stock Error", JOptionPane.ERROR_MESSAGE); return;
        }
        Order newOrder = new Order(orderId, username, userId, selectedPayment, totalAmount); newOrder.setItems(orderItems);
        String transactionId;
        java.util.concurrent.ExecutorService paymentExecutor = Executors.newSingleThreadExecutor();
        try {
            PaymentResult paymentResult = paymentExecutor.submit(new PaymentTask(newOrder, toPaymentType(selectedPayment))).get();
            if (!paymentResult.isSuccess()) { restoreReservedStock(stockAdjusted); JOptionPane.showMessageDialog(this, paymentResult.getMessage(), "Payment Failed", JOptionPane.ERROR_MESSAGE); return; }
            transactionId = paymentResult.getTransactionId();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); restoreReservedStock(stockAdjusted); JOptionPane.showMessageDialog(this, "Payment was interrupted. Stock has been restored.", "Checkout Error", JOptionPane.ERROR_MESSAGE); return;
        } catch (java.util.concurrent.ExecutionException ex) {
            restoreReservedStock(stockAdjusted); JOptionPane.showMessageDialog(this, "Payment failed: " + ex.getCause().getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE); return;
        } finally { paymentExecutor.shutdownNow(); }
        OrderService.createOrder(newOrder);
        refreshOpenOrderViews();
        new Thread(new ecommerce.concurrency.NotificationTask(newOrder), "shopsphere-order-notification").start();
        String successMsg = "Payment processed successfully!\nOrder ID: " + orderId + "\nPayment Method: " + selectedPayment + "\nTransaction: " + transactionId;
        if (!couponCode.isEmpty()) successMsg += "\nCoupon applied successfully!";
        JOptionPane.showMessageDialog(this, successMsg, "Payment Processed", JOptionPane.INFORMATION_MESSAGE);
        cart.clear(); saveAllCarts(); refreshCartTable(); refreshAdminCartsTable(); refreshOrdersTable();
    }

    private static PaymentType toPaymentType(String paymentLabel) {
        if (paymentLabel == null) return PaymentType.CREDIT_CARD;
        switch (paymentLabel) {
            case "PayPal": return PaymentType.PAYPAL;
            case "Net Banking": return PaymentType.NET_BANKING;
            case "Cash on Delivery": return PaymentType.CASH_ON_DELIVERY;
            default: return PaymentType.CREDIT_CARD;
        }
    }

    private void restoreReservedStock(List<ecommerce.cart.CartItem> items) {
        for (ecommerce.cart.CartItem item : items) {
            try {
                AppData.INVENTORY_SERVICE.restoreStock(item.getProductId(), item.getQuantity());
            } catch (ProductNotFoundException ignored) {
                // A product deleted during checkout cannot be restored; report via service logs if needed.
            }
        }
    }

    private boolean addProductToCart(Product product) {
        synchronized (CARTS_BY_USER) {
            CartItem existing = cart.stream().filter(i -> i.getProduct().getId() == product.getId()).findFirst().orElse(null);
            int requested = (existing == null ? 0 : existing.getQuantity()) + 1;
            if (product.getStock() <= 0 || requested > product.getStock()) {
                JOptionPane.showMessageDialog(this, product.getName() + " has only " + product.getStock() + " unit(s) available.", "Stock Limit", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (existing == null) cart.add(new CartItem(product, 1)); else existing.setQuantity(requested);
            saveAllCarts();
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<CartItem>> loadSavedCarts() {
        File file = new File("data/carts_by_user.dat");
        if (!file.isFile()) return new LinkedHashMap<>();
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            Object value = in.readObject();
            if (value instanceof Map<?, ?>) return (Map<String, List<CartItem>>) value;
        } catch (Exception ex) { System.err.println("Could not load saved carts: " + ex.getMessage()); }
        return new LinkedHashMap<>();
    }

    private static void saveAllCarts() {
        synchronized (CARTS_BY_USER) {
            File file = new File("data/carts_by_user.dat");
            File dir = file.getParentFile(); if (dir != null) dir.mkdirs();
            File temp = new File(file.getPath() + ".tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(temp)))) {
                out.writeObject(new LinkedHashMap<>(CARTS_BY_USER));
            } catch (IOException ex) { System.err.println("Could not save carts: " + ex.getMessage()); return; }
            if (!temp.renameTo(file)) {
                try { java.nio.file.Files.move(temp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
                catch (IOException ex) { System.err.println("Could not replace saved carts: " + ex.getMessage()); }
            }
            javax.swing.SwingUtilities.invokeLater(() -> {
                for (MainApp app : OPEN_APPS) {
                    app.refreshCartTable();
                    app.refreshAdminCartsTable();
                }
            });
        }
    }

    private static void refreshOpenOrderViews() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            for (MainApp app : OPEN_APPS) app.refreshOrdersTable();
        });
    }

    private JPanel createAdminCartsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK); panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel heading = new JLabel("Customer Cart Overview (read-only)"); heading.setForeground(TEXT_LIGHT); heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(heading, BorderLayout.NORTH);
        adminCartsTableModel = new DefaultTableModel(new String[]{"Customer", "Product ID", "Product", "Category", "Qty", "Unit Price", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(adminCartsTableModel); styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = createStyledButton("Refresh Carts", ACCENT_PURPLE, new Dimension(140, 34));
        refresh.addActionListener(e -> refreshAdminCartsTable());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT)); footer.setOpaque(false); footer.add(refresh); panel.add(footer, BorderLayout.SOUTH);
        refreshAdminCartsTable(); return panel;
    }

    private void refreshAdminCartsTable() {
        if (adminCartsTableModel == null) return;
        adminCartsTableModel.setRowCount(0);
        synchronized (CARTS_BY_USER) {
            for (Map.Entry<String, List<CartItem>> entry : CARTS_BY_USER.entrySet()) {
                for (CartItem item : entry.getValue()) {
                    Product p = item.getProduct();
                    adminCartsTableModel.addRow(new Object[]{entry.getKey(), p.getId(), p.getName(), p.getCategory(), item.getQuantity(), p.getFormattedRupeePrice(), java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(p.getPriceAsDouble() * item.getQuantity())});
                }
            }
        }
    }

    private void styleTable(JTable table) {
        table.setBackground(PANEL_DARK); table.setForeground(TEXT_LIGHT); table.setGridColor(BORDER_COLOR); table.setRowHeight(28);
        table.getTableHeader().setBackground(PANEL_DARK); table.getTableHeader().setForeground(TEXT_LIGHT);
    }

    private void refreshCartTable() {
        if (cartTableModel != null) {
            cartTableModel.setRowCount(0);
            for (CartItem item : cart) {
                cartTableModel.addRow(new Object[]{
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getCategory(),
                    item.getProduct().getFormattedRupeePrice(),
                    item.getQuantity()
                });
            }
        }
    }

    // ==================== ORDERS & FULFILLMENT PANEL ====================
    private DefaultTableModel newOrderTableModel() {
        return new DefaultTableModel(new String[]{"Order ID", "Customer", "Seller", "Items", "Total (INR)", "Status", "Tracking"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JTable buildOrderTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        styleTable(table);
        table.setRowHeight(30);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                c.setBackground(PANEL_DARK); c.setForeground(TEXT_LIGHT);
                c.setFont(new Font("SansSerif", Font.BOLD, 12));
                return c;
            }
        });
        return table;
    }

    private JPanel createCustomerOrdersPanel(boolean completedOnly) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK); panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel title = new JLabel(completedOnly ? "Completed Orders" : "Waiting / Active Orders");
        title.setForeground(TEXT_LIGHT); title.setFont(new Font("SansSerif", Font.BOLD, 17));
        panel.add(title, BorderLayout.NORTH);

        DefaultTableModel model = newOrderTableModel();
        if (completedOnly) completedOrdersTableModel = model; else ordersTableModel = model;
        JTable table = buildOrderTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4)); actions.setOpaque(false);
        JButton refresh = createStyledButton("Refresh", ACCENT_PURPLE, new Dimension(110, 36));
        refresh.addActionListener(e -> refreshOrdersTable());
        actions.add(refresh);
        if (!completedOnly) {
            JButton cancel = createStyledButton("Cancel Selected Order", DANGER_RED, new Dimension(190, 36));
            cancel.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
                String id = String.valueOf(model.getValueAt(row, 0));
                try {
                    OrderService.cancelOrder(id);
                    refreshOpenOrderViews();
                    JOptionPane.showMessageDialog(this, "Order " + id + " cancelled and eligible stock restored.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE); }
            });
            actions.add(cancel);
        }
        panel.add(actions, BorderLayout.SOUTH);
        refreshOrdersTable();
        return panel;
    }

    private JPanel createSellerOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK); panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel title = new JLabel("Orders for " + username + " — Dispatch or Cancel Your Products");
        title.setForeground(TEXT_LIGHT); title.setFont(new Font("SansSerif", Font.BOLD, 17));
        panel.add(title, BorderLayout.NORTH);

        ordersTableModel = newOrderTableModel();
        JTable table = buildOrderTable(ordersTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4)); actions.setOpaque(false);
        JButton dispatch = createStyledButton("Dispatch Order", SUCCESS_GREEN, new Dimension(150, 36));
        JButton cancel = createStyledButton("Cancel Order", DANGER_RED, new Dimension(140, 36));
        JButton refresh = createStyledButton("Refresh", ACCENT_PURPLE, new Dimension(110, 36));
        dispatch.addActionListener(e -> sellerFulfillSelectedOrder(table, true));
        cancel.addActionListener(e -> sellerFulfillSelectedOrder(table, false));
        refresh.addActionListener(e -> refreshOrdersTable());
        actions.add(dispatch); actions.add(cancel); actions.add(refresh);
        panel.add(actions, BorderLayout.SOUTH);
        refreshOrdersTable();
        return panel;
    }

    private void sellerFulfillSelectedOrder(JTable table, boolean dispatch) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        String orderId = String.valueOf(ordersTableModel.getValueAt(row, 0));
        try {
            if (dispatch) {
                OrderService.dispatchSellerOrder(orderId, userId, username);
                JOptionPane.showMessageDialog(this, "Order " + orderId + " has been dispatched by your store.", "Order Dispatched", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int answer = JOptionPane.showConfirmDialog(this, "Cancel your store's portion of " + orderId + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) return;
                OrderService.cancelSellerOrder(orderId, userId, username);
                JOptionPane.showMessageDialog(this, "Your store's portion of " + orderId + " was cancelled and stock was restored.", "Order Cancelled", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshOpenOrderViews();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Order Action Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createOrdersPanel() {
        // Admin-only system order monitor.
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK); panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel title = new JLabel("All System Orders"); title.setForeground(TEXT_LIGHT); title.setFont(new Font("SansSerif", Font.BOLD, 17));
        panel.add(title, BorderLayout.NORTH);
        ordersTableModel = newOrderTableModel();
        JTable table = buildOrderTable(ordersTableModel); panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = createStyledButton("Refresh", ACCENT_PURPLE, new Dimension(110, 36)); refresh.addActionListener(e -> refreshOrdersTable());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(refresh); panel.add(actions, BorderLayout.SOUTH);
        refreshOrdersTable(); return panel;
    }

    private List<Order> getVisibleOrders() {
        if ("ADMIN".equalsIgnoreCase(role)) return OrderService.getAllOrders();
        if ("SELLER".equalsIgnoreCase(role)) return OrderService.getOrdersForSeller(userId, username);
        return OrderService.getOrdersByUser(userId);
    }

    private void addOrderRow(DefaultTableModel model, Order o) {
        String sellerDisplay = o.getSellerStatuses().keySet().stream().map(this::sellerDisplayName).collect(Collectors.joining(", "));
        String items;
        if (o.getItems() == null) {
            items = "";
        } else if ("SELLER".equalsIgnoreCase(role)) {
            items = o.getItems().stream()
                    .filter(i -> i != null && i.getProduct() != null &&
                            (userId.equalsIgnoreCase(i.getProduct().getSellerId()) ||
                             username.equalsIgnoreCase(i.getProduct().getSellerId()) ||
                             username.equalsIgnoreCase(i.getProduct().getSellerName())))
                    .map(i -> i.getProductName() + " × " + i.getQuantity())
                    .collect(Collectors.joining(", "));
            if (items.isBlank()) items = "Your store's products in this order";
        } else {
            items = o.getItems().stream()
                    .map(i -> i.getProductName() + " × " + i.getQuantity()).collect(Collectors.joining(", "));
        }
        String status = "SELLER".equalsIgnoreCase(role) ? o.getSellerStatus(userId).name() : o.getStatus().name();
        model.addRow(new Object[]{o.getOrderId(), o.getCustomerName(), sellerDisplay, items,
                java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(o.getTotalPaid()),
                status, o.getTrackingStatus()});
    }

    private String sellerDisplayName(String sellerId) {
        return AppData.USER_REPOSITORY.findById(sellerId).map(u -> u.getUsername()).orElse(sellerId);
    }

    private void refreshOrdersTable() {
        if (ordersTableModel != null) ordersTableModel.setRowCount(0);
        if (completedOrdersTableModel != null) completedOrdersTableModel.setRowCount(0);

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            for (Order o : getVisibleOrders()) {
                if (o.getStatus() == ecommerce.orders.OrderStatus.DELIVERED) {
                    if (completedOrdersTableModel != null) addOrderRow(completedOrdersTableModel, o);
                } else {
                    if (ordersTableModel != null) addOrderRow(ordersTableModel, o);
                }
            }
        } else {
            for (Order o : getVisibleOrders()) if (ordersTableModel != null) addOrderRow(ordersTableModel, o);
        }
    }

    // ==================== SELLER INVENTORY PANEL ====================
    private JPanel createSellerInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Store Inventory Control - Add New Product");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_LIGHT);
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBackground(PANEL_DARK);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField();
        JComboBox<String> catCombo = new JComboBox<>(CATEGORIES);
        catCombo.setBackground(BG_DARK);
        catCombo.setForeground(TEXT_LIGHT);
        catCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        catCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? ACCENT_PURPLE : BG_DARK);
                c.setForeground(TEXT_LIGHT);
                c.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return c;
            }
        });

        JTextField priceField = new JTextField();
        JTextField descField = new JTextField();
        final java.io.File[] selectedImage = {null};
        JButton chooseImageBtn = createStyledButton("Choose Product Image...", ACCENT_PURPLE, new Dimension(190, 32));
        JLabel imageStatus = new JLabel("No image selected (category artwork will be used)"); imageStatus.setForeground(TEXT_MUTED);
        chooseImageBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Product images (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { selectedImage[0] = chooser.getSelectedFile(); imageStatus.setText(selectedImage[0].getName()); }
        });

        formPanel.add(new JLabel("Product Name:") {{ setForeground(TEXT_LIGHT); }});
        formPanel.add(nameField);
        formPanel.add(new JLabel("Category:") {{ setForeground(TEXT_LIGHT); }});
        formPanel.add(catCombo);
        formPanel.add(new JLabel("Price (₹ INR):") {{ setForeground(TEXT_LIGHT); }});
        formPanel.add(priceField);
        formPanel.add(new JLabel("Description:") {{ setForeground(TEXT_LIGHT); }});
        formPanel.add(descField);
        formPanel.add(new JLabel("Product Image:") {{ setForeground(TEXT_LIGHT); }});
        JPanel imagePickerPanel = new JPanel(new BorderLayout(6, 0)); imagePickerPanel.setOpaque(false); imagePickerPanel.add(chooseImageBtn, BorderLayout.WEST); imagePickerPanel.add(imageStatus, BorderLayout.CENTER); formPanel.add(imagePickerPanel);

        JButton addProductBtn = createStyledButton("Add Product to Catalog", SUCCESS_GREEN, new Dimension(200, 35));
        addProductBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String category = (String) catCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                String desc = descField.getText().trim();
                int newId = globalProductList.stream().mapToInt(Product::getId).max().orElse(0) + 1;

                if (name.isEmpty() || desc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (price <= 0) { JOptionPane.showMessageDialog(this, "Price must be greater than ₹0.", "Invalid Price", JOptionPane.WARNING_MESSAGE); return; }
                Product newProduct = new Product(newId, name, category, price, desc, Color.GRAY);
                newProduct.setCurrencyCode("INR");
                if ("SELLER".equalsIgnoreCase(role)) { newProduct.setSellerId(userId); newProduct.setSellerName(username); }
                if (selectedImage[0] != null) {
                    java.io.File imageDir = new java.io.File("assets/product-images"); imageDir.mkdirs();
                    String ext = "png"; String fileName = selectedImage[0].getName(); int dot = fileName.lastIndexOf('.'); if (dot >= 0) ext = fileName.substring(dot + 1).toLowerCase();
                    java.io.File destination = new java.io.File(imageDir, "seller_product_" + newId + "." + ext);
                    java.nio.file.Files.copy(selectedImage[0].toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    newProduct.setImagePath(destination.getPath());
                } else newProduct.setImagePath("assets/product-images/product_" + newId + ".png");
                globalProductList.add(newProduct);
                AppData.PRODUCT_REPOSITORY.save(newProduct);
                AppData.FILE_MANAGER.saveProducts(AppData.PRODUCT_REPOSITORY.findAll());
                refreshSellerProductsTable();

                JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                priceField.setText("");
                descField.setText(""); selectedImage[0] = null; imageStatus.setText("No image selected (category artwork will be used)");
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save product image: " + ex.getMessage(), "Image Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.setBackground(BG_DARK);
        formContainer.add(formPanel, BorderLayout.CENTER);
        formContainer.add(addProductBtn, BorderLayout.SOUTH);

        JPanel lower = new JPanel(new BorderLayout(8, 8)); lower.setBackground(BG_DARK);
        JLabel ownProductsHeading = new JLabel("ADMIN".equalsIgnoreCase(role) ? "All Store Products" : "Products Added by " + username); ownProductsHeading.setForeground(TEXT_LIGHT); ownProductsHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        lower.add(ownProductsHeading, BorderLayout.NORTH);
        sellerProductsTableModel = new DefaultTableModel(new String[]{"ID", "Product", "Category", "Price", "Stock", "Image"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable ownProducts = new JTable(sellerProductsTableModel); styleTable(ownProducts);
        ownProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton editStockBtn = createStyledButton("Update Selected Product Stock", ACCENT_PURPLE, new Dimension(250, 36));
        editStockBtn.addActionListener(e -> {
            int selectedRow = ownProducts.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Select one of your products first.", "Select Product", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = ownProducts.convertRowIndexToModel(selectedRow);
            int productId = ((Number) sellerProductsTableModel.getValueAt(modelRow, 0)).intValue();
            Product selectedProduct = AppData.PRODUCT_REPOSITORY.findById(productId).orElse(null);
            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(this, "This product no longer exists. Refresh the inventory.", "Product Not Found", JOptionPane.ERROR_MESSAGE);
                refreshSellerProductsTable();
                return;
            }
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            boolean ownsProduct = userId.equalsIgnoreCase(selectedProduct.getSellerId())
                    || username.equalsIgnoreCase(selectedProduct.getSellerName());
            if (!isAdmin && !ownsProduct) {
                JOptionPane.showMessageDialog(this, "You can only update stock for products belonging to your store.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String entered = JOptionPane.showInputDialog(this,
                    "Available stock for " + selectedProduct.getName() + " (current: " + selectedProduct.getStock() + "):",
                    Integer.toString(selectedProduct.getStock()));
            if (entered == null) return;
            try {
                int newStock = Integer.parseInt(entered.trim());
                if (newStock < 0) throw new NumberFormatException("negative stock");
                AppData.INVENTORY_SERVICE.updateStock(productId, newStock);
                refreshSellerProductsTable();
                refreshCatalogStockDisplays();
                JOptionPane.showMessageDialog(this, "Stock updated successfully. New available quantity: " + newStock,
                        "Inventory Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a whole number greater than or equal to 0.", "Invalid Stock", JOptionPane.WARNING_MESSAGE);
            } catch (ProductNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Product Not Found", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel productTablePanel = new JPanel(new BorderLayout(8, 8));
        productTablePanel.setOpaque(false);
        productTablePanel.add(new JScrollPane(ownProducts), BorderLayout.CENTER);
        JPanel stockActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        stockActions.setOpaque(false);
        stockActions.add(editStockBtn);
        productTablePanel.add(stockActions, BorderLayout.SOUTH);
        lower.add(productTablePanel, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formContainer, lower); split.setResizeWeight(0.55); split.setBorder(null); split.setBackground(BG_DARK);
        panel.add(split, BorderLayout.CENTER); refreshSellerProductsTable();
        return panel;
    }

    private void refreshCatalogStockDisplays() {
        // Refresh product/catalog panels in this window, where applicable. The source of truth
        // is the repository, so reopening a product detail view always reads the current stock.
        repaint();
    }

    private void refreshSellerProductsTable() {
        if (sellerProductsTableModel == null) return;
        sellerProductsTableModel.setRowCount(0);
        List<Product> products = AppData.PRODUCT_REPOSITORY.findAll().stream()
                .filter(p -> "ADMIN".equalsIgnoreCase(role) || userId.equalsIgnoreCase(p.getSellerId()) || username.equalsIgnoreCase(p.getSellerName()))
                .collect(Collectors.toList());
        for (Product p : products) sellerProductsTableModel.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), p.getFormattedRupeePrice(), p.getStock(), p.getImagePath() == null ? "Artwork" : new File(p.getImagePath()).getName()});
    }

    // ==================== HELPER UI COMPONENTS ====================
    private JButton createStyledButton(String text, Color baseColor, Dimension size) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ==================== INITIALIZING PRODUCTS ====================
    private static void initializeProducts() {
        java.util.List<Product> savedProducts = AppData.PRODUCT_REPOSITORY.findAll();
        if (savedProducts.size() >= 20) {
            for (Product saved : savedProducts) {
                // Migrate products from the original prototype, whose stored prices were USD.
                if (saved.getCurrencyCode() == null || !"INR".equalsIgnoreCase(saved.getCurrencyCode())) {
                    saved.setPrice(saved.getPriceAsDouble() * 86.5);
                    saved.setCurrencyCode("INR");
                }
                if (saved.getImagePath() == null || saved.getImagePath().isBlank())
                    saved.setImagePath("assets/product-images/product_" + saved.getId() + ".png");
                if (saved.getSellerId() == null || saved.getSellerId().isBlank()) {
                    int sellerIndex = globalProductList.size() % 3;
                    String[] ids = {"USR-002", "USR-005", "USR-006"};
                    String[] names = {"seller1", "seller2", "seller3"};
                    saved.setSellerId(ids[sellerIndex]);
                    saved.setSellerName(names[sellerIndex]);
                }
                globalProductList.add(saved);
                AppData.PRODUCT_REPOSITORY.save(saved);
            }
            AppData.FILE_MANAGER.saveProducts(AppData.PRODUCT_REPOSITORY.findAll());
            return;
        }
        int idCounter = 1;

        String[] elecNames = {"Wireless Headphones", "Smartwatch Pro", "Mechanical Keyboard", "Gaming Mouse", 
            "Ultrawide Monitor", "4K Webcam", "Portable SSD 1TB", "Bluetooth Speaker", 
            "USB-C Multiport Hub", "Wireless Charging Pad", "Noise Cancelling Earbuds", "Streaming Microphone"};
        for (String name : elecNames) {
            globalProductList.add(new Product(idCounter++, name, "Electronics", (49.99 + (idCounter * 5)) * 86.5, "High performance electronic device.", Color.GRAY));
        }

        String[] furnNames = {"Ergonomic Chair", "Standing Desk", "Minimalist Bookshelf", "Modern Executive Desk", 
            "Adjustable Footrest", "Leather Recliner", "Monitor Riser Stand", "Filing Cabinet", 
            "Gaming Chair", "Foldable Study Table", "LED Floor Lamp", "Side Accent Table"};
        for (String name : furnNames) {
            globalProductList.add(new Product(idCounter++, name, "Furniture", (39.99 + (idCounter * 4)) * 86.5, "Premium modern furniture piece.", Color.GRAY));
        }

        String[] clothNames = {"Classic Cotton T-Shirt", "Slim Fit Jeans", "Waterproof Bomber Jacket", "Leather Winter Boots", 
            "Running Sneakers", "Formal Blazer", "Casual Hoodie", "Designer Sunglasses", 
            "Silk Necktie", "Woolen Scarf", "Athletic Joggers", "Formal Dress Shirt"};
        for (String name : clothNames) {
            globalProductList.add(new Product(idCounter++, name, "Clothing & Fashion", (19.99 + (idCounter * 3)) * 86.5, "Trendy fashion apparel.", Color.GRAY));
        }

        String[] bookNames = {"Java Programming Guide", "Data Structures Handbook", "Sci-Fi Epic Novel", "Leather Journal Notebook", 
            "Gel Ink Pens (Pack of 12)", "Minimalist Desk Planner", "Art Sketchbook", "Business Management Primer", 
            "Self-Improvement Guide", "World Atlas 2026 Edition", "Mechanical Pencil Set", "Sticky Notes Assortment"};
        for (String name : bookNames) {
            globalProductList.add(new Product(idCounter++, name, "Books & Stationery", (14.99 + (idCounter * 2)) * 86.5, "Educational and professional reading material.", Color.GRAY));
        }

        String[] sportNames = {"Yoga Mat Pro", "Adjustable Dumbbell Set", "Mountain Bike", "Insulated Water Bottle", 
            "Camping Tent 4-Person", "LED Headlamp", "Resistance Bands Set", "Running Hydration Vest", 
            "Hiking Backpack", "Tennis Racket", "Basketball Official Size", "Fitness Tracker Watch"};
        for (String name : sportNames) {
            globalProductList.add(new Product(idCounter++, name, "Sports & Outdoors", (24.99 + (idCounter * 3.5)) * 86.5, "Durable gear for sports and adventure.", Color.GRAY));
        }

        String[] homeNames = {"Stainless Steel Cookware Set", "Air Fryer 6Qt", "Drip Coffee Maker", "Ergonomic Chef Knife", 
            "Non-Stick Frying Pan", "Blender for Smoothies", "Electric Kettle", "Microfiber Bed Sheets", 
            "Memory Foam Pillow", "Cast Iron Skillet", "Scented Soy Candle", "Reusable Food Storage Bags"};
        for (String name : homeNames) {
            globalProductList.add(new Product(idCounter++, name, "Home & Kitchen", (29.99 + (idCounter * 3)) * 86.5, "Essential goods for your home and kitchen.", Color.GRAY));
        }

        String[] beautyNames = {"Organic Facial Cleanser", "Hydrating Moisturizer", "Vitamin C Serum", "Ionic Hair Dryer", 
            "Electric Toothbrush", "Aromatherapy Essential Oil Diffuser", "Exfoliating Body Scrub", "Matte Lipstick Set", 
            "Nail Care Grooming Kit", "SPF 50 Sunscreen Lotion", "Hair Styling Clay", "Under-Eye Collagen Patches"};
        for (String name : beautyNames) {
            globalProductList.add(new Product(idCounter++, name, "Beauty & Personal Care", (18.99 + (idCounter * 2.5)) * 86.5, "Rejuvenating care and beauty items.", Color.GRAY));
        }

        String[] toyNames = {"Building Blocks City Set", "Remote Control Drone", "Board Game Strategy Classic", "Plush Teddy Bear", 
            "Rubik's Speed Cube", "Action Figure Hero", "Educational Science Kit", "Jigsaw Puzzle 1000 Pieces", 
            "Interactive Robot Toy", "Outdoor Frisbee Disc", "Inflatable Pool Float", "Watercolor Paint Set"};
        for (String name : toyNames) {
            globalProductList.add(new Product(idCounter++, name, "Toys & Games", (12.99 + (idCounter * 2.2)) * 86.5, "Fun entertainment for all ages.", Color.GRAY));
        }

        String[] groceryNames = {"Organic Green Tea Bags", "Mixed Nuts Premium Pack", "Multivitamin Capsules", "Whey Protein Powder", 
            "Extra Virgin Olive Oil", "Dark Chocolate Truffles", "Raw Wildflower Honey", "Organic Granola Cereal", 
            "Immune Booster Supplement", "Himalayan Pink Salt", "Chia Seeds Organic", "Instant Espresso Coffee"};
        for (String name : groceryNames) {
            globalProductList.add(new Product(idCounter++, name, "Groceries & Health", (9.99 + (idCounter * 1.8)) * 86.5, "Healthy provisions and daily groceries.", Color.GRAY));
        }
        // Every seeded product gets its own local image asset and INR currency marker.
        for (Product product : globalProductList) {
            product.setImagePath("assets/product-images/product_" + product.getId() + ".png");
            product.setCurrencyCode("INR");
        }
        // Assign the existing catalog across three demo sellers.
        String[] sellerIds = {"USR-002", "USR-005", "USR-006"};
        String[] sellerNames = {"seller1", "seller2", "seller3"};
        for (int i = 0; i < globalProductList.size(); i++) {
            Product product = globalProductList.get(i);
            if (product.getSellerId() == null || product.getSellerId().isBlank()) {
                int sellerIndex = i % sellerIds.length;
                product.setSellerId(sellerIds[sellerIndex]);
                product.setSellerName(sellerNames[sellerIndex]);
            }
        }
        // Keep the displayed catalog and persistent repository in sync.
        AppData.PRODUCT_REPOSITORY.clear();
        for (Product product : globalProductList) AppData.PRODUCT_REPOSITORY.save(product);
        AppData.FILE_MANAGER.saveProducts(AppData.PRODUCT_REPOSITORY.findAll());
    }
}