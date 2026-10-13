package ecommerce.cart;

import java.io.Serializable;
import java.math.BigDecimal;

public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private BigDecimal discountPercentage; // e.g. 0.15 for 15%
    private BigDecimal minimumAmount;
    private boolean active;

    public Coupon() {
        this.active = true;
    }

    public Coupon(String code, BigDecimal discountPercentage, BigDecimal minimumAmount, boolean active) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.minimumAmount = minimumAmount;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
