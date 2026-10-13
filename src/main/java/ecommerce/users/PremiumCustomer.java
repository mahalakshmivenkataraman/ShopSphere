package ecommerce.users;

import java.math.BigDecimal;

public class PremiumCustomer extends Customer {
    private static final long serialVersionUID = 1L;

    private BigDecimal membershipDiscountRate;

    public PremiumCustomer() {
        super();
        this.membershipDiscountRate = new BigDecimal("0.10"); // 10% default discount
    }

    public PremiumCustomer(String id, String username, String password, String address, BigDecimal membershipDiscountRate) {
        super(id, username, password, address);
        this.membershipDiscountRate = membershipDiscountRate;
    }

    public BigDecimal getMembershipDiscountRate() {
        return membershipDiscountRate;
    }

    public void setMembershipDiscountRate(BigDecimal membershipDiscountRate) {
        this.membershipDiscountRate = membershipDiscountRate;
    }
}
