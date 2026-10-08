using Microsoft.Extensions.Time.Testing;

namespace CampusShop.Tests;

public class CouponServiceTests
{
    [Fact]
    public void IsUsable_ValidCoupon_ReturnsTrue()
    {
        // Arrange
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.Valid();

        // Act
        var result = service.IsUsable(coupon, 150m);

        // Assert
        Assert.True(result);
    }

    [Fact]
    public void IsUsable_ExpiredCoupon_ReturnsFalse()
    {
        // Arrange
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.ExpiredYesterday();

        // Act
        var result = service.IsUsable(coupon, 150m);

        // Assert
        Assert.False(result);
    }

    [Fact]
    public void IsUsable_BelowThreshold_ReturnsFalse()
    {
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.Valid();

        var result = service.IsUsable(coupon, 99.99m);

        Assert.False(result);
    }

    [Fact]
    public void IsUsable_ExactlyThreshold_ReturnsTrue()
    {
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.Valid();

        var result = service.IsUsable(coupon, 100m);

        Assert.True(result);
    }

    // ---- 判定表（需求 3.1）：未过期 × 达到门槛 × 未使用 ----

    [Fact]
    public void IsUsable_AlreadyUsed_ReturnsFalse()
    {
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.ExpiresInDays(30, used: true);

        var result = service.IsUsable(coupon, 150m);

        Assert.False(result);
    }

    [Fact]
    public void IsUsable_LastDayOfValidity_ReturnsTrue()
    {
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.LastDayToday();

        var result = service.IsUsable(coupon, 150m);

        Assert.True(result);
    }

    [Fact]
    public void Apply_UsableCoupon_SubtractsAmount()
    {
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.Valid();

        var result = service.Apply(coupon, 150m);

        Assert.Equal(130m, result);
    }

    [Fact]
    public void Apply_UnusableCoupon_ReturnsOriginalAmount()
    {
        var service = new CouponService(CouponFactory.FixedTime());
        var coupon = CouponFactory.ExpiredYesterday();

        var result = service.Apply(coupon, 150m);

        Assert.Equal(150m, result);
    }

    // ---- 需求 3.4：“今天”按北京时间计算 ----

    [Fact]
    public void IsUsable_LastDayLateNightBeijing_ReturnsTrue()
    {
        // 北京时间 10-08 23:30，即 UTC 10-08 15:30
        var time = new FakeTimeProvider(new DateTimeOffset(2026, 10, 8, 23, 30, 0, TimeSpan.FromHours(8)));
        var service = new CouponService(time);

        var result = service.IsUsable(CouponFactory.LastDayToday(), 150m);

        Assert.True(result);
    }

    [Fact]
    public void IsUsable_DayAfterExpiryEarlyMorningBeijing_ReturnsFalse()
    {
        // 北京时间 10-09 00:30，即 UTC 10-08 16:30：按 UTC 算还是 10-08，按北京时间已过期
        var time = new FakeTimeProvider(new DateTimeOffset(2026, 10, 9, 0, 30, 0, TimeSpan.FromHours(8)));
        var service = new CouponService(time);

        var result = service.IsUsable(CouponFactory.LastDayToday(), 150m);

        Assert.False(result);
    }
}
