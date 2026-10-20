package campusshop;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 运费规则。实现见 docs/requirements.md 2.7。
 */
@Component
public class ShippingPolicy {

    private static final int FREE_SHIPPING_THRESHOLD = 3;
    private static final BigDecimal FREE = new BigDecimal("0.00");
    private static final BigDecimal FEE = new BigDecimal("10.00");

    public BigDecimal feeFor(int totalQuantity) {
        if (totalQuantity < 0)
            throw new IllegalArgumentException("总件数不能为负：" + totalQuantity);
        return isFree(totalQuantity) ? FREE : FEE;
    }

    private static boolean isFree(int totalQuantity) {
        return totalQuantity == 0 || totalQuantity >= FREE_SHIPPING_THRESHOLD;
    }
}
