namespace CampusShop;

/// <summary>
/// 优惠券规则。见 docs/requirements.md 第 3 节。
/// </summary>
public class CouponService
{
    private static readonly TimeSpan BeijingOffset = TimeSpan.FromHours(8);

    private readonly TimeProvider _time;

    /// <summary>
    /// time 提供“现在”：生产环境传入 TimeProvider.System，测试中传入固定时间。
    /// </summary>
    public CouponService(TimeProvider time) => _time = time;

    /// <summary>
    /// 判断优惠券能否用于金额为 amount 的订单。
    /// </summary>
    public bool IsUsable(Coupon c, decimal amount)
    {
        var today = Today();
        if (today > c.ExpireDate)
            return false;
        if (amount < c.Threshold)
            return false;
        return !c.IsUsed;
    }

    /// <summary>
    /// 使用优惠券后的应付金额；优惠券不可用时返回原金额。
    /// </summary>
    public decimal Apply(Coupon c, decimal amount)
    {
        if (!IsUsable(c, amount))
            return amount;
        return amount - c.Amount;
    }

    /// <summary>北京时间的今天（需求 3.4），与服务器所在时区无关。</summary>
    private DateOnly Today() =>
        DateOnly.FromDateTime(_time.GetUtcNow().ToOffset(BeijingOffset).DateTime);
}
