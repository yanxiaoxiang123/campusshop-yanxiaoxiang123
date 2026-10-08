namespace CampusShop;

/// <summary>优惠券使用次数限制。见 docs/requirements.md 3.5。</summary>
public class CouponUsageService(TimeProvider time)
{
    public const int MaxUsesPerDay = 2;

    private readonly Dictionary<(string, DateOnly), int> _used = new();

    /// <summary>尝试使用一张优惠券；超过当天次数限制时返回 false。</summary>
    public bool TryUse(string userId)
    {
        var key = (userId, Today());
        _used.TryGetValue(key, out var count);
        if (count >= MaxUsesPerDay) return false;
        _used[key] = count + 1;
        return true;
    }

    /// <summary>北京时间的今天（需求 3.4）。</summary>
    private DateOnly Today() =>
        DateOnly.FromDateTime(time.GetUtcNow().ToOffset(TimeSpan.FromHours(8)).DateTime);
}
