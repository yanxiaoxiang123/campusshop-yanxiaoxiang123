using Microsoft.Extensions.Time.Testing;

namespace CampusShop.Tests;

/// <summary>
/// 测试用的优惠券：满 100 减 20。“今天”固定为 2026-10-08（北京时间），不受运行测试的机器影响。
/// </summary>
public static class CouponFactory
{
    public static readonly DateOnly Today = new(2026, 10, 8);

    /// <summary>固定时间：2026-10-08 10:00（北京时间）。</summary>
    public static FakeTimeProvider FixedTime() =>
        new(new DateTimeOffset(2026, 10, 8, 10, 0, 0, TimeSpan.FromHours(8)));

    public static Coupon ExpiresInDays(int days, decimal threshold = 100m, bool used = false) => new()
    {
        Code = "TEST",
        Threshold = threshold,
        Amount = 20m,
        ExpireDate = Today.AddDays(days),
        IsUsed = used,
    };

    /// <summary>一个月后过期。</summary>
    public static Coupon Valid() => ExpiresInDays(30);

    /// <summary>今天是有效期最后一天。</summary>
    public static Coupon LastDayToday() => ExpiresInDays(0);

    /// <summary>昨天已过期。</summary>
    public static Coupon ExpiredYesterday() => ExpiresInDays(-1);
}
