package ecommerce.cart;

import ecommerce.products.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private Product product;

    public CartItem() {
    }

    public CartItem(int productId, String productName, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public CartItem(Product product, int quantity) {
        if (product != null) {
            this.product = product;
            this.productId = product.getId();
            this.productName = product.getName();
            this.unitPrice = product.getPrice();
        }
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName != null ? productName : (product != null ? product.getName() : "");
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice != null ? unitPrice : (product != null ? product.getPrice() : BigDecimal.ZERO);
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        if (product == null) {
            product = new Product(productId, productName, "Category", getUnitPrice().doubleValue(), "Product description", null);
        }
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getId();
            this.productName = product.getName();
            this.unitPrice = product.getPrice();
        }
    }

    public BigDecimal subtotal() {
        BigDecimal priceToUse = getUnitPrice();
        if (priceToUse == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return priceToUse.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return productId == cartItem.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId=" + productId +
                ", productName='" + getProductName() + '\'' +
                ", unitPrice=" + getUnitPrice() +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal() +
                '}';
    }
}
