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

    /** 旧调用：只算商品金额，不含运费。新代码请使用三参数版本。 */
    public BigDecimal totalFor(User user, BigDecimal subtotal) {
        return calc.calculateTotal(subtotal, user.level());
    }

    /** 商品金额按会员折扣、满减计算后，加上按总件数计算的运费。 */
    public BigDecimal totalFor(User user, BigDecimal subtotal, int totalQuantity) {
        throw new UnsupportedOperationException("含运费计价尚未实现");
    }

    /** 按总件数返回运费。 */
    public BigDecimal shippingFor(int totalQuantity) {
        throw new UnsupportedOperationException("含运费计价尚未实现");
    }
}
