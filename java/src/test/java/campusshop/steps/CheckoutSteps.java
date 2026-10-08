package campusshop.steps;

import campusshop.MemberLevel;
import campusshop.PriceCalculator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckoutSteps {

    private MemberLevel level;
    private BigDecimal original, total;

    @Given("用户是 {string} 会员")
    public void member(String name) { level = MemberLevel.fromChineseName(name); }

    @Given("购物车商品原价为 {int} 元")
    public void original(int amount) { original = BigDecimal.valueOf(amount); }

    @When("用户结算")
    public void checkout() {
        total = new PriceCalculator().calculateTotal(original, level);
    }

    @Then("应付金额为 {bigdecimal} 元")
    public void expectTotal(BigDecimal expected) {
        assertEquals(0, expected.compareTo(total), () -> "expected " + expected + " but was " + total);
    }
}
