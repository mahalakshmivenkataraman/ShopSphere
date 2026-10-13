package ecommerce.coupon;

import ecommerce.exceptions.InvalidCouponException;

import java.math.BigDecimal;

public class CouponService {

    public static double applyCoupon(String code, double subtotal) throws InvalidCouponException {
        if (code == null || code.trim().isEmpty()) {
            return subtotal;
        }

        String normalizedCode = code.trim().toUpperCase();
        double discount = 0.0;

        switch (normalizedCode) {
            case "SAVE10":
                discount = subtotal * 0.10;
                break;
            case "SAVE20":
                discount = subtotal * 0.20;
                break;
            case "FLAT50":
                discount = 50.0;
                break;
            default:
                throw new InvalidCouponException("Invalid or expired coupon code: " + code);
        }

        double finalAmount = subtotal - discount;
        return finalAmount < 0 ? 0.0 : finalAmount;
    }

    public static BigDecimal applyCoupon(String code, BigDecimal subtotal) throws InvalidCouponException {
        if (subtotal == null) return BigDecimal.ZERO;
        double subDouble = subtotal.doubleValue();
        double finalDouble = applyCoupon(code, subDouble);
        return BigDecimal.valueOf(finalDouble);
    }
}
