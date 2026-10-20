package campusshop.steps;

import campusshop.MemberLevel;
import campusshop.PriceCalculator;
import campusshop.ShippingPolicy;
import campusshop.User;
import campusshop.web.Pricing;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** 满 3 件包邮的 BDD 步骤：在领域层直接调用计价对象，不启动网站。 */
public class ShippingSteps {

    private final Pricing pricing = new Pricing(new PriceCalculator(), new ShippingPolicy());

    private BigDecimal subtotal;
    private MemberLevel level;
    private int quantity;
    private BigDecimal shipping;
    private BigDecimal total;

    @Given("购物车小计为 {bigdecimal} 元")
    public void cartSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Given("用户是普通会员")
    public void normalMember() {
        this.level = MemberLevel.NORMAL;
    }

    @Given("用户是银卡会员")
    public void silverMember() {
        this.level = MemberLevel.SILVER;
    }

    @Given("用户是金卡会员")
    public void goldMember() {
        this.level = MemberLevel.GOLD;
    }

    @Given("购物车中有 {int} 件商品")
    public void cartHas(int quantity) {
        this.quantity = quantity;
    }

    @When("我结算购物车")
    public void checkout() {
        User user = new User("alice", "123456", "学生公寓", "13800000001", level);
        this.shipping = pricing.shippingFor(quantity);
        this.total = pricing.totalFor(user, subtotal, quantity);
    }

    @Then("运费应为 {bigdecimal} 元")
    public void shippingShouldBe(BigDecimal expected) {
        assertThat(shipping).isEqualByComparingTo(expected);
    }

    @Then("应付金额应为 {bigdecimal} 元")
    public void totalShouldBe(BigDecimal expected) {
        assertThat(total).isEqualByComparingTo(expected);
    }
}
