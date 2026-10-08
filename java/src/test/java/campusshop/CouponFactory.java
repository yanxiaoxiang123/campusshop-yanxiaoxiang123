package campusshop;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 测试用的优惠券：满 100 减 20。“今天”固定为 2026-10-08（北京时间），不受运行测试的机器影响。
 */
public final class CouponFactory {

    public static final LocalDate TODAY = LocalDate.of(2026, 10, 8);

    private CouponFactory() {
    }

    /** 固定时钟：2026-10-08 10:00（北京时间）。 */
    public static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-10-08T02:00:00Z"), ZoneId.of("Asia/Shanghai"));
    }

    public static Coupon expiresInDays(int days, BigDecimal threshold, boolean used) {
        return new Coupon("TEST", threshold, new BigDecimal("20"), TODAY.plusDays(days), used);
    }

    public static Coupon expiresInDays(int days) {
        return expiresInDays(days, new BigDecimal("100"), false);
    }

    /** 一个月后过期。 */
    public static Coupon valid() {
        return expiresInDays(30);
    }

    /** 今天是有效期最后一天。 */
    public static Coupon lastDayToday() {
        return expiresInDays(0);
    }

    /** 昨天已过期。 */
    public static Coupon expiredYesterday() {
        return expiresInDays(-1);
    }
}
