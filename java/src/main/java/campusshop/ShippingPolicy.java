package campusshop;

import java.math.BigDecimal;

/**
 * 运费规则。实现见 docs/requirements.md 2.7。
 */
public class ShippingPolicy {

    public BigDecimal feeFor(int totalQuantity) {
        if (totalQuantity >= 3)
            return new BigDecimal("0.00");
        throw new UnsupportedOperationException("不足 3 件尚未实现");
    }
}
