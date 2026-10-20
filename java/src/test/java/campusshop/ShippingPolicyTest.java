package campusshop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingPolicyTest {

    private final ShippingPolicy policy = new ShippingPolicy();

    @Test
    void feeFor_threeItems_isZero() {
        assertEquals(0, new BigDecimal("0.00").compareTo(policy.feeFor(3)));
    }

    @Test
    void feeFor_fourItems_isZero() {
        assertEquals(0, new BigDecimal("0.00").compareTo(policy.feeFor(4)));
    }
}
