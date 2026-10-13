package ecommerce.gui;

import ecommerce.main.AppData;
import ecommerce.users.Admin;
import ecommerce.users.Customer;
import ecommerce.users.Seller;
import ecommerce.users.User;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    // Helper User Class to store Password and Role
    public static class UserAccount {
        private String password;
        private String role; // "CUSTOMER", "SELLER", "ADMIN"

        public UserAccount(String password, String role) {
            this.password = password;
            this.role = role;
        }

        public String getPassword() {
            return password;
        }

        public String getRole() {
            return role;
        }
    }

    // In-memory User Database (Username -> UserAccount)
    private static final Map<String, UserAccount> userDatabase = new HashMap<>();

    static {
        // Keep the original demo credentials available, then load saved accounts.
        userDatabase.put("Alex", new UserAccount("password123", "CUSTOMER"));
        userDatabase.put("seller1", new UserAccount("seller123", "SELLER"));
        userDatabase.put("admin", new UserAccount("admin123", "ADMIN"));
        for (User user : AppData.USER_REPOSITORY.findAll()) {
            userDatabase.put(user.getUsername(), new UserAccount(user.getPassword(), user.getRoleString()));
        }
    }

    private static void saveRegisteredAccount(String username, String password, String role) {
        String id = "USR-" + System.currentTimeMillis();
        User user;
        switch (role) {
            case "SELLER": user = new Seller(id, username, password, username + " Store"); break;
            case "ADMIN": user = new Admin(id, username, password); break;
            default: user = new Customer(id, username, password, "Not provided"); break;
        }
        AppData.USER_REPOSITORY.save(user);
        AppData.FILE_MANAGER.saveUsers(AppData.USER_REPOSITORY.findAll());
        userDatabase.put(username, new UserAccount(password, role));
    }

    private final Color BG_DARK = new Color(15, 23, 42);
    private final Color PANEL_DARK = new Color(30, 41, 59);
    private final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private final Color TEXT_LIGHT = new Color(248, 250, 252);
    private final Color TEXT_MUTED = new Color(148, 163, 184);
    private final Color BORDER_COLOR = new Color(51, 65, 85);
    private final Color SUCCESS_GREEN = new Color(34, 197, 94);

    public LoginFrame() {
        setTitle("ShopSphere - Login & Register");
        setSize(900, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(BG_DARK);

        // LEFT BRANDING PANEL
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(88, 28, 135), 0, getHeight(), BG_DARK);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(60, 45, 60, 45));

        JLabel logo = new JLabel("ShopSphere");
        logo.setFont(new Font("SansSerif", Font.BOLD, 36));
        logo.setForeground(TEXT_LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>Experience next-gen online shopping<br>in sleek dark mode.</html>");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tagline.setForeground(TEXT_MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("🛍️");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 90));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(logo);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(tagline);
        leftPanel.add(Box.createVerticalStrut(40));
        leftPanel.add(icon);

        // RIGHT LOGIN FORM PANEL
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcome.setForeground(TEXT_LIGHT);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue your shopping");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        usernameLabel.setForeground(TEXT_LIGHT);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        passwordLabel.setForeground(TEXT_LIGHT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginButton = createStyledButton("Sign In", ACCENT_PURPLE);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel registerLabel = new JLabel(
                "<html>Don't have an account? <font color='#8B5CF6'><b>Register here</b></font></html>");
        registerLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        registerLabel.setForeground(TEXT_MUTED);
        registerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegistrationModal();
            }
        });

        rightPanel.add(welcome);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(subtitle);
        rightPanel.add(Box.createVerticalStrut(28));
        rightPanel.add(usernameLabel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(18));
        rightPanel.add(passwordLabel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(26));
        rightPanel.add(loginButton);
        rightPanel.add(Box.createVerticalStrut(18));
        rightPanel.add(registerLabel);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    private void openRegistrationModal() {
        JDialog regDialog = new JDialog(this, "Create Account", true);
        regDialog.setSize(420, 520);
        regDialog.setLocationRelativeTo(this);
        regDialog.getContentPane().setBackground(BG_DARK);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel title = new JLabel("Register Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_LIGHT);

        JLabel nameLbl = new JLabel("Username");
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLbl.setForeground(TEXT_LIGHT);
        JTextField regUserField = createStyledTextField();

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLbl.setForeground(TEXT_LIGHT);
        JPasswordField regPassField = createStyledPasswordField();

        JLabel roleLbl = new JLabel("Account Type");
        roleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        roleLbl.setForeground(TEXT_LIGHT);

        JComboBox<String> roleCombo = new JComboBox<>(new String[] { "CUSTOMER", "SELLER", "ADMIN" });
        roleCombo.setBackground(PANEL_DARK);
        roleCombo.setForeground(TEXT_LIGHT);
        roleCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Custom renderer ensuring account type text/dropdown options are visible and
        // themed correctly
        roleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(ACCENT_PURPLE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(PANEL_DARK);
                    c.setForeground(TEXT_LIGHT);
                }
                return c;
            }
        });

        JButton submitBtn = createStyledButton("Create Account", SUCCESS_GREEN);

        submitBtn.addActionListener(e -> {
            String newUsername = regUserField.getText().trim();
            String newPassword = new String(regPassField.getPassword());
            String selectedRole = (String) roleCombo.getSelectedItem();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(regDialog, "Please enter all fields.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userDatabase.containsKey(newUsername)) {
                JOptionPane.showMessageDialog(regDialog, "Username already exists.", "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            userDatabase.put(newUsername, new UserAccount(newPassword, selectedRole));
            JOptionPane.showMessageDialog(regDialog, "Registration successful!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            regDialog.dispose();
            usernameField.setText(newUsername);
            passwordField.setText("");
        });

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(title, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(nameLbl, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(regUserField, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(passLbl, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(regPassField, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(roleLbl, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(roleCombo, gbc);
        gbc.gridy = 7;
        panel.add(submitBtn, gbc);

        regDialog.add(panel);
        regDialog.setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        setupInputComponent(field);
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        setupInputComponent(field);
        return field;
    }

    private void setupInputComponent(JTextField field) {
        Dimension fieldSize = new Dimension(310, 42);
        field.setPreferredSize(fieldSize);
        field.setMaximumSize(fieldSize);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(PANEL_DARK);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(TEXT_LIGHT);

        RoundedBorder border = new RoundedBorder(10, BORDER_COLOR);
        field.setBorder(new CompoundBorder(border, new EmptyBorder(0, 12, 0, 12)));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                border.setColor(ACCENT_PURPLE);
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                border.setColor(BORDER_COLOR);
                field.repaint();
            }
        });
    }

    private JButton createStyledButton(String text, Color baseColor) {
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
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();

                super.paintComponent(g);
            }
        };

        Dimension buttonSize = new Dimension(310, 44);
        button.setPreferredSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Login Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDatabase.containsKey(username) && userDatabase.get(username).getPassword().equals(password)) {
            UserAccount account = userDatabase.get(username);
            // Pass username AND user role to MainApp
            MainApp app = new MainApp(username, account.getRole());
            app.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Authentication Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}