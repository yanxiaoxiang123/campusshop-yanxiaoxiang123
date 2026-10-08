package campusshop;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * 优惠券规则。见 docs/requirements.md 第 3 节。
 */
public class CouponService {

    private static final ZoneOffset BEIJING = ZoneOffset.ofHours(8);

    private final Clock clock;

    /**
     * clock 提供“现在”：生产环境传入 Clock.systemDefaultZone()，测试中传入固定时钟。
     */
    public CouponService(Clock clock) {
        this.clock = clock;
    }

    /**
     * 判断优惠券能否用于金额为 amount 的订单。
     */
    public boolean isUsable(Coupon c, BigDecimal amount) {
        LocalDate today = today();
        if (today.isAfter(c.getExpireDate()))
            return false;
        if (amount.compareTo(c.getThreshold()) < 0)
            return false;
        return !c.isUsed();
    }

    /**
     * 使用优惠券后的应付金额；优惠券不可用时返回原金额。
     */
    public BigDecimal apply(Coupon c, BigDecimal amount) {
        if (!isUsable(c, amount))
            return amount;
        return amount.subtract(c.getAmount());
    }

    /** 北京时间的今天（需求 3.4），与服务器所在时区无关。 */
    private LocalDate today() {
        return LocalDate.now(clock.withZone(BEIJING));
    }
}
