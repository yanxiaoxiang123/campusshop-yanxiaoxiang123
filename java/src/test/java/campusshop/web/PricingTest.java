package campusshop.web;

import campusshop.MemberLevel;
import campusshop.PriceCalculator;
import campusshop.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.MonthDay;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 需求 2.7：按用户生日和北京时间判断是否享受额外九折。 */
class PricingTest {

    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");

    // 北京时间 2026-10-08 10:00
    private static final Clock OCT_8_BEIJING =
            Clock.fixed(Instant.parse("2026-10-08T02:00:00Z"), BEIJING);

    // UTC 2026-10-07 16:30 = 北京时间 2026-10-08 00:30（跨日边界）
    private static final Clock OCT_7_UTC =
            Clock.fixed(Instant.parse("2026-10-07T16:30:00Z"), ZoneId.of("UTC"));

    private static User alice(MonthDay birthday) {
        return new User("alice", "123456", "学生公寓 3 栋 402", "13800000001",
                MemberLevel.NORMAL, birthday);
    }

    @Test
    void totalFor_birthday99_returns89_10() {
        Pricing pricing = new Pricing(new PriceCalculator(), OCT_8_BEIJING);

        BigDecimal total = pricing.totalFor(alice(MonthDay.of(10, 8)), new BigDecimal("99"));

        assertEquals(0, new BigDecimal("89.10").compareTo(total),
                () -> "生日当天应额外九折，实际 " + total);
    }

    @Test
    void totalFor_notBirthday99_returns99() {
        Pricing pricing = new Pricing(new PriceCalculator(), OCT_8_BEIJING);

        BigDecimal total = pricing.totalFor(alice(MonthDay.of(10, 9)), new BigDecimal("99"));

        assertEquals(0, new BigDecimal("99.00").compareTo(total),
                () -> "不是生日不应额外打折，实际 " + total);
    }

    @Test
    void totalFor_birthdayInBeijing_utcClockPreviousDay_stillGetsDiscount() {
        Pricing pricing = new Pricing(new PriceCalculator(), OCT_7_UTC);

        BigDecimal total = pricing.totalFor(alice(MonthDay.of(10, 8)), new BigDecimal("99"));

        assertEquals(0, new BigDecimal("89.10").compareTo(total),
                () -> "UTC 仍是 10-07，但北京时间已是 10-08，应按生日处理，实际 " + total);
    }

    @Test
    void totalFor_noBirthday_returns99() {
        Pricing pricing = new Pricing(new PriceCalculator(), OCT_8_BEIJING);

        BigDecimal total = pricing.totalFor(alice(null), new BigDecimal("99"));

        assertEquals(0, new BigDecimal("99.00").compareTo(total),
                () -> "没有生日资料不应享受生日折扣，实际 " + total);
    }

    // ---- 需求 2.7.5：2 月 29 日生日在非闰年的处理 ----

    private static final Clock FEB_28_2027_BEIJING =
            Clock.fixed(Instant.parse("2027-02-28T02:00:00Z"), BEIJING);

    private static final Clock MAR_1_2027_BEIJING =
            Clock.fixed(Instant.parse("2027-03-01T02:00:00Z"), BEIJING);

    @Test
    void totalFor_feb29Birthday_nonLeapYearFeb28_getsDiscount() {
        Pricing pricing = new Pricing(new PriceCalculator(), FEB_28_2027_BEIJING);

        BigDecimal total = pricing.totalFor(alice(MonthDay.of(2, 29)), new BigDecimal("99"));

        assertEquals(0, new BigDecimal("89.10").compareTo(total),
                () -> "非闰年 2 月 28 日应按生日处理，实际 " + total);
    }

    @Test
    void totalFor_feb29Birthday_nonLeapYearMar1_noDiscount() {
        Pricing pricing = new Pricing(new PriceCalculator(), MAR_1_2027_BEIJING);

        BigDecimal total = pricing.totalFor(alice(MonthDay.of(2, 29)), new BigDecimal("99"));

        assertEquals(0, new BigDecimal("99.00").compareTo(total),
                () -> "非闰年 3 月 1 日不应再享受生日折扣，实际 " + total);
    }
}
