package campusshop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void feeFor_oneItem_isTen() {
        assertEquals(0, new BigDecimal("10.00").compareTo(policy.feeFor(1)));
    }

    @Test
    void feeFor_twoItems_isTen() {
        assertEquals(0, new BigDecimal("10.00").compareTo(policy.feeFor(2)));
    }

    @Test
    void feeFor_zeroItems_isZero() {
        assertEquals(0, new BigDecimal("0.00").compareTo(policy.feeFor(0)));
    }

    @Test
    void feeFor_negativeQuantity_throws() {
        assertThrows(IllegalArgumentException.class, () -> policy.feeFor(-1));
    }
}
