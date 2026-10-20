package campusshop;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 运费规则。实现见 docs/requirements.md 2.7。
 */
@Component
public class ShippingPolicy {

    public BigDecimal feeFor(int totalQuantity) {
        if (totalQuantity < 0)
            throw new IllegalArgumentException("总件数不能为负：" + totalQuantity);
        if (totalQuantity == 0 || totalQuantity >= 3)
            return new BigDecimal("0.00");
        return new BigDecimal("10.00");
    }
}
