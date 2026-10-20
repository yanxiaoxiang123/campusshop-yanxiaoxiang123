package campusshop.web;

import campusshop.PriceCalculator;
import campusshop.ShippingPolicy;
import campusshop.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 计算某个用户的应付金额。 */
@Component
public class Pricing {

    private final PriceCalculator calc;
    private final ShippingPolicy shipping;

    public Pricing(PriceCalculator calc, ShippingPolicy shipping) {
        this.calc = calc;
        this.shipping = shipping;
    }

    /** 商品金额按会员折扣、满减计算后，加上按总件数计算的运费。 */
    public BigDecimal totalFor(User user, BigDecimal subtotal, int totalQuantity) {
        return calc.calculateTotal(subtotal, user.level()).add(shipping.feeFor(totalQuantity));
    }

    /** 按总件数返回运费。 */
    public BigDecimal shippingFor(int totalQuantity) {
        return shipping.feeFor(totalQuantity);
    }
}
