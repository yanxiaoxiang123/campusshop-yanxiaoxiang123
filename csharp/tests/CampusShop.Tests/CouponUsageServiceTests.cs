using Microsoft.Extensions.Time.Testing;

namespace CampusShop.Tests;

/// <summary>需求 3.5：每人每天限用 2 张优惠券。</summary>
public class CouponUsageServiceTests
{
    private static FakeTimeProvider FixedTime() =>
        new(new DateTimeOffset(2026, 10, 8, 10, 0, 0, TimeSpan.FromHours(8)));

    [Fact]
    public void TryUse_ThirdCouponSameDay_Rejected()
    {
        // Arrange：时间固定在北京时间 10 月 8 日 10:00
        var svc = new CouponUsageService(FixedTime());
        svc.TryUse("alice");
        svc.TryUse("alice");

        // Act
        var third = svc.TryUse("alice");

        // Assert
        Assert.False(third);
    }

    [Fact]
    public void TryUse_FirstTwoSameDay_Allowed()
    {
        var svc = new CouponUsageService(FixedTime());

        Assert.True(svc.TryUse("alice"));
        Assert.True(svc.TryUse("alice"));
    }

    [Fact]
    public void TryUse_NextDay_CountResets()
    {
        var time = FixedTime();
        var svc = new CouponUsageService(time);
        svc.TryUse("alice");
        svc.TryUse("alice");

        time.Advance(TimeSpan.FromDays(1));    // 前进一天

        Assert.True(svc.TryUse("alice"));
    }

    [Fact]
    public void TryUse_DifferentUsers_CountedSeparately()
    {
        var svc = new CouponUsageService(FixedTime());
        svc.TryUse("alice");
        svc.TryUse("alice");

        Assert.True(svc.TryUse("bob"));
    }

    [Fact]
    public void Refund_AfterCancel_AllowsAnotherUse()
    {
        var svc = new CouponUsageService(FixedTime());
        svc.TryUse("alice");
        svc.TryUse("alice");

        svc.Refund("alice");          // 订单取消，返还一次

        Assert.True(svc.TryUse("alice"));
    }

    [Fact]
    public void Refund_NothingUsed_DoesNotAllowExtraUse()
    {
        var svc = new CouponUsageService(FixedTime());

        svc.Refund("alice");          // 没用过，返还不产生额外次数

        Assert.True(svc.TryUse("alice"));
        Assert.True(svc.TryUse("alice"));
        Assert.False(svc.TryUse("alice"));
    }
}
