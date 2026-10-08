package campusshop;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/** 优惠券使用次数限制。见 docs/requirements.md 3.5。 */
public class CouponUsageService {

    public static final int MAX_USES_PER_DAY = 2;

    private final Clock clock;
    private final Map<String, Integer> used = new HashMap<>();

    public CouponUsageService(Clock clock) {
        this.clock = clock;
    }

    /** 尝试使用一张优惠券；超过当天次数限制时返回 false。 */
    public boolean tryUse(String userId) {
        String key = key(userId);
        int count = used.getOrDefault(key, 0);
        if (count >= MAX_USES_PER_DAY) return false;
        used.put(key, count + 1);
        return true;
    }

    private String key(String userId) {
        return userId + "|" + today();
    }

    /** 北京时间的今天（需求 3.4）。 */
    private LocalDate today() {
        return LocalDate.now(clock.withZone(ZoneOffset.ofHours(8)));
    }
}
