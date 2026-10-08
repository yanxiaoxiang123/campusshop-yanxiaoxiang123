namespace CampusShop.Specs.StepDefinitions;

[Binding]
public class CheckoutSteps
{
    private MemberLevel _level;
    private decimal _original, _total;

    [Given("用户是 {string} 会员")]
    public void GivenMember(string name) => _level = name switch
    {
        "普通" => MemberLevel.Normal, "银卡" => MemberLevel.Silver,
        "金卡" => MemberLevel.Gold, _ => throw new ArgumentException(name)
    };

    [Given("购物车商品原价为 {int} 元")]
    public void GivenOriginal(int amount) => _original = amount;

    [When("用户结算")]
    public void WhenCheckout() =>
        _total = new PriceCalculator().CalculateTotal(_original, _level);

    [Then(@"^应付金额为 ([\d.]+) 元$")]
    public void ThenTotal(decimal expected) => Assert.Equal(expected, _total);
}
