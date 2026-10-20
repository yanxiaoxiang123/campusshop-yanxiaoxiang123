package campusshop.web;

import campusshop.MemberLevel;
import campusshop.PriceCalculator;
import campusshop.ShippingPolicy;
import campusshop.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingTest {

    private final Pricing pricing = new Pricing(new PriceCalculator(), new ShippingPolicy());

    private static User user(MemberLevel level) {
        return new User("alice", "123456", "学生公寓", "13800000001", level);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    @Test
    void totalFor_twoItems_normal_addsShipping() {
        // 100 - 满减 10 = 90；2 件运费 10；应付 100.00
        assertMoney("100.00", pricing.totalFor(user(MemberLevel.NORMAL), new BigDecimal("100.00"), 2));
    }

    @Test
    void totalFor_threeItems_normal_freeShipping() {
        // 100 - 满减 10 = 90；3 件免运费；应付 90.00
        assertMoney("90.00", pricing.totalFor(user(MemberLevel.NORMAL), new BigDecimal("100.00"), 3));
    }

    @Test
    void totalFor_twoItems_gold_discountThenFullReductionThenShipping() {
        // 119.70 × 0.9 = 107.73；满 100 减 10 = 97.73；2 件运费 10；应付 107.73
        assertMoney("107.73", pricing.totalFor(user(MemberLevel.GOLD), new BigDecimal("119.70"), 2));
    }

    @Test
    void totalFor_twoItems_shippingNotCountedInFullReduction() {
        // 92.40 未达满减门槛；2 件运费 10；应付 102.40，而不是 92.40
        assertMoney("102.40", pricing.totalFor(user(MemberLevel.NORMAL), new BigDecimal("92.40"), 2));
    }

    @Test
    void totalFor_twoItems_silver_shippingNotDiscounted() {
        // 110 × 0.95 = 104.50；满 100 减 10 = 94.50；2 件运费 10；应付 104.50
        assertMoney("104.50", pricing.totalFor(user(MemberLevel.SILVER), new BigDecimal("110.00"), 2));
    }

    @Test
    void totalFor_emptyCart_isZero() {
        assertMoney("0.00", pricing.totalFor(user(MemberLevel.NORMAL), new BigDecimal("0.00"), 0));
    }

    @Test
    void shippingFor_threeItems_isZero() {
        assertMoney("0.00", pricing.shippingFor(3));
    }

    @Test
    void shippingFor_twoItems_isTen() {
        assertMoney("10.00", pricing.shippingFor(2));
    }
}
