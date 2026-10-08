package campusshop;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** 测试用的时钟：可以手动“前进”，相当于 C# 的 FakeTimeProvider.Advance。 */
final class MutableClock extends Clock {

    private Clock current;

    MutableClock(Clock start) {
        this.current = start;
    }

    void advance(Duration d) {
        current = Clock.offset(current, d);
    }

    @Override
    public ZoneId getZone() {
        return current.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return current.withZone(zone);
    }

    @Override
    public Instant instant() {
        return current.instant();
    }
}
