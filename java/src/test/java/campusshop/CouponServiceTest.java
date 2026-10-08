package campusshop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponServiceTest {

    @Test
    void isUsable_validCoupon_returnsTrue() {
        // Arrange
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.valid();

        // Act
        boolean result = service.isUsable(coupon, new BigDecimal("150"));

        // Assert
        assertTrue(result);
    }

    @Test
    void isUsable_expiredCoupon_returnsFalse() {
        // Arrange
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.expiredYesterday();

        // Act
        boolean result = service.isUsable(coupon, new BigDecimal("150"));

        // Assert
        assertFalse(result);
    }

    @Test
    void isUsable_belowThreshold_returnsFalse() {
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.valid();

        boolean result = service.isUsable(coupon, new BigDecimal("99.99"));

        assertFalse(result);
    }

    @Test
    void isUsable_exactlyThreshold_returnsTrue() {
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.valid();

        boolean result = service.isUsable(coupon, new BigDecimal("100"));

        assertTrue(result);
    }

    // ---- 判定表（需求 3.1）：未过期 × 达到门槛 × 未使用 ----

    @Test
    void isUsable_alreadyUsed_returnsFalse() {
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.expiresInDays(30, new BigDecimal("100"), true);

        boolean result = service.isUsable(coupon, new BigDecimal("150"));

        assertFalse(result);
    }

    @Test
    void isUsable_lastDayOfValidity_returnsTrue() {
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.lastDayToday();

        boolean result = service.isUsable(coupon, new BigDecimal("150"));

        assertTrue(result);
    }

    @Test
    void apply_usableCoupon_subtractsAmount() {
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.valid();

        BigDecimal result = service.apply(coupon, new BigDecimal("150"));

        assertEquals(0, new BigDecimal("130").compareTo(result));
    }

    @Test
    void apply_unusableCoupon_returnsOriginalAmount() {
        CouponService service = new CouponService(CouponFactory.fixedClock());
        Coupon coupon = CouponFactory.expiredYesterday();

        BigDecimal result = service.apply(coupon, new BigDecimal("150"));

        assertEquals(0, new BigDecimal("150").compareTo(result));
    }

    // ---- 需求 3.4：“今天”按北京时间计算 ----

    @Test
    void isUsable_lastDayLateNightBeijing_returnsTrue() {
        // 北京时间 10-08 23:30，即 UTC 10-08 15:30
        Clock clock = Clock.fixed(Instant.parse("2026-10-08T15:30:00Z"), ZoneOffset.UTC);
        CouponService service = new CouponService(clock);

        boolean result = service.isUsable(CouponFactory.lastDayToday(), new BigDecimal("150"));

        assertTrue(result);
    }

    @Test
    void isUsable_dayAfterExpiryEarlyMorningBeijing_returnsFalse() {
        // 北京时间 10-09 00:30，即 UTC 10-08 16:30：按 UTC 算还是 10-08，按北京时间已过期
        Clock clock = Clock.fixed(Instant.parse("2026-10-08T16:30:00Z"), ZoneOffset.UTC);
        CouponService service = new CouponService(clock);

        boolean result = service.isUsable(CouponFactory.lastDayToday(), new BigDecimal("150"));

        assertFalse(result);
    }
}
