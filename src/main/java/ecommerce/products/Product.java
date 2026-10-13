package ecommerce.products;

import java.awt.Color;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private String imagePath;
    private String sellerId;
    private String sellerName;
    // Null on products serialized by the earlier USD-based version; used for one-time migration.
    private String currencyCode = "INR";
    private transient Color accentColor;

    public Product() {
        this.active = true;
        this.stock = 50;
        this.price = BigDecimal.ZERO;
        this.accentColor = Color.GRAY;
    }

    public Product(int id, String name, String description, String category, BigDecimal price, int stock, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.stock = stock;
        this.active = active;
        this.accentColor = Color.GRAY;
    }

    public Product(int id, String name, String description, String category, BigDecimal price, int stock, boolean active, Color accentColor) {
        this(id, name, description, category, price, stock, active);
        this.accentColor = accentColor;
    }

    // Constructor used by Person 1 & 2 Swing GUI
    public Product(int id, String name, String category, double price, String description, Color accentColor) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = BigDecimal.valueOf(price);
        this.description = description;
        this.accentColor = accentColor;
        this.stock = 50;
        this.active = true;
    }

    // Currency calculation & formatting methods
    public double getPriceInRupees() {
        return getPriceAsDouble();
    }

    public String getFormattedRupeePrice() {
        NumberFormat inrFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
        return inrFormat.format(getPriceInRupees());
    }

    public String getImagePath() { return imagePath; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public double getPriceAsDouble() {
        return price != null ? price.doubleValue() : 0.0;
    }

    // Standard Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setPrice(double price) { this.price = BigDecimal.valueOf(price); }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Color getAccentColor() { return accentColor != null ? accentColor : Color.GRAY; }
    public void setAccentColor(Color accentColor) { this.accentColor = accentColor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", active=" + active +
                '}';
    }
}
