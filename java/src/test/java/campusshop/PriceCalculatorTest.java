package campusshop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTest {

    @Test
    void applyDiscount_totalIs0_returns0() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("0"));

        assertEquals(0, new BigDecimal("0").compareTo(result));
    }

    @Test
    void applyDiscount_totalIs50_returns50() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("50"));

        assertEquals(0, new BigDecimal("50").compareTo(result));
    }

    @Test
    void applyDiscount_totalIs99_returns99() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("99"));

        assertEquals(0, new BigDecimal("99").compareTo(result));
    }

    @Test
    void applyDiscount_totalIs120_returns110() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("120"));

        assertEquals(0, new BigDecimal("110").compareTo(result));
    }

    @Test
    void applyDiscount_totalIs150_returns140() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("150"));

        assertEquals(0, new BigDecimal("140").compareTo(result));
    }

    @Test
    void applyDiscount_totalIs180_returns170() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("180"));

        assertEquals(0, new BigDecimal("170").compareTo(result));
    }

    @ParameterizedTest
    @CsvSource({
            "200,    170",    // 正好 200：减 30
            "250,    220",
            "199.99, 189.99"  // 不足 200：仍按满 100 减 10
    })
    void applyDiscount_tier200_minus30(BigDecimal total, BigDecimal expected) {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(total);

        assertEquals(0, expected.compareTo(result));
    }

    // ---- calculateTotal：需求第 2 节 ----

    @ParameterizedTest
    @CsvSource({
            "99.99,  NORMAL, 99.99",    // 边界 100 左
            "100,    NORMAL, 90.00",    // 边界 100
            "200,    NORMAL, 170.00",   // 边界 200
            "200,    GOLD,   170.00",   // 交互：200 × 0.9 = 180 → 减 10
            "110,    SILVER, 94.50",    // 交互：110 × 0.95 = 104.5 → 减 10
            "105,    SILVER, 99.75"     // 交互：105 × 0.95 = 99.75，跌破门槛
    })
    void calculateTotal_validInput_returnsExpected(BigDecimal total, MemberLevel level, BigDecimal expected) {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(total, level);

        assertEquals(0, expected.compareTo(result), () -> "expected " + expected + " but was " + result);
    }

    @ParameterizedTest
    @CsvSource({"-0.01", "100000.01"})
    void calculateTotal_outOfRange_throws(BigDecimal total) {
        PriceCalculator calc = new PriceCalculator();

        assertThrows(IllegalArgumentException.class,
                () -> calc.calculateTotal(total, MemberLevel.NORMAL));
    }

    @Test
    void applyDiscount_totalIs100_returns90() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.applyDiscount(new BigDecimal("100"));

        assertEquals(0, new BigDecimal("90").compareTo(result));
    }

    // ---- 第 8 周：针对存活的变异体补充的测试 ----

    @ParameterizedTest
    @CsvSource({
            "0,      NORMAL, 0.00",      // 需求 2.1：0 是合法下限（杀死 < → <=）
            "100000, NORMAL, 99970.00",  // 需求 2.1：100000 是合法上限（杀死 > → >=）
            "133.33, SILVER, 116.66",    // 需求 2.2：133.33×0.95=126.6635→126.66→减10→116.66
            "150,    GOLD,   125.00",    // 需求 2.2：150×0.9=135→减10→125
            "10.10,  SILVER, 9.60"       // 需求 2.2：10.10×0.95=9.595→四舍五入→9.60
    })
    void calculateTotal_boundaryAndRounding_returnsExpected(BigDecimal total, MemberLevel level, BigDecimal expected) {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(total, level);

        assertEquals(0, expected.compareTo(result), () -> "expected " + expected + " but was " + result);
    }

    // ---- 第 13 周：会员生日当天额外九折（需求 2.7） ----

    @Test
    void calculateTotal_birthdayNormal99_returns89_10() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(new BigDecimal("99"), MemberLevel.NORMAL, true);

        assertEquals(0, new BigDecimal("89.10").compareTo(result),
                () -> "生日九折后应为 89.10，实际 " + result);
    }

    @Test
    void calculateTotal_notBirthdayNormal99_returns99() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(new BigDecimal("99"), MemberLevel.NORMAL, false);

        assertEquals(0, new BigDecimal("99.00").compareTo(result));
    }

    @Test
    void calculateTotal_birthdayGold200_returns152() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(new BigDecimal("200"), MemberLevel.GOLD, true);

        // 200 × 0.9（金卡）= 180.00；180 × 0.9（生日）= 162.00；162 ≥ 100，减 10 → 152.00
        assertEquals(0, new BigDecimal("152.00").compareTo(result),
                () -> "金卡生日折扣叠加后再满减应为 152.00，实际 " + result);
    }

    @Test
    void calculateTotal_birthdaySilver110_returns94_05() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(new BigDecimal("110"), MemberLevel.SILVER, true);

        // 110 × 0.95 = 104.50；× 0.9 = 94.05；94.05 < 100，不再满减
        assertEquals(0, new BigDecimal("94.05").compareTo(result),
                () -> "银卡生日折扣后应跌破满减门槛，实际 " + result);
    }

    @Test
    void calculateTotal_birthdayNormal200_appliesBirthdayThenFullReduction() {
        PriceCalculator calc = new PriceCalculator();

        BigDecimal result = calc.calculateTotal(new BigDecimal("200"), MemberLevel.NORMAL, true);

        // 200 × 0.9 = 180.00；180 ≥ 100，减 10 → 170.00
        assertEquals(0, new BigDecimal("170.00").compareTo(result),
                () -> "生日折扣后再满减应为 170.00，实际 " + result);
    }
}
