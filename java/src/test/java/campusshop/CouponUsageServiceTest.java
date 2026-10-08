package campusshop;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 需求 3.5：每人每天限用 2 张优惠券。 */
class CouponUsageServiceTest {

    // 北京时间 2026-10-08 10:00
    private final Clock t0 = Clock.fixed(Instant.parse("2026-10-08T02:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void tryUse_thirdCouponSameDay_rejected() {
        // Arrange
        CouponUsageService svc = new CouponUsageService(t0);
        svc.tryUse("alice");
        svc.tryUse("alice");

        // Act
        boolean third = svc.tryUse("alice");

        // Assert
        assertFalse(third);
    }

    @Test
    void tryUse_firstTwoSameDay_allowed() {
        CouponUsageService svc = new CouponUsageService(t0);

        assertTrue(svc.tryUse("alice"));
        assertTrue(svc.tryUse("alice"));
    }
}
