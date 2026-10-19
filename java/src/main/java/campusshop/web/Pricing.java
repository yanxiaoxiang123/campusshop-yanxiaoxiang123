package campusshop.web;

import campusshop.PriceCalculator;
import campusshop.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;

/** 计算某个用户的应付金额。 */
@Component
public class Pricing {

    /** 生日按北京时间判断，与服务器时区无关（需求 2.7）。 */
    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");

    private final PriceCalculator calc;
    private final Clock clock;

    public Pricing(PriceCalculator calc, Clock clock) {
        this.calc = calc;
        this.clock = clock;
    }

    public BigDecimal totalFor(User user, BigDecimal subtotal) {
        LocalDate today = LocalDate.ofInstant(clock.instant(), BEIJING);
        boolean birthday = isBirthday(user.birthday(), today);
        return calc.calculateTotal(subtotal, user.level(), birthday);
    }

    private static boolean isBirthday(MonthDay birthday, LocalDate today) {
        if (birthday == null)
            return false;
        MonthDay todayMonthDay = MonthDay.from(today);
        if (birthday.equals(todayMonthDay))
            return true;
        // 需求 2.7.5：2 月 29 日生日在非闰年按 2 月 28 日处理
        return birthday.equals(MonthDay.of(2, 29))
                && !today.isLeapYear()
                && todayMonthDay.equals(MonthDay.of(2, 28));
    }
}
