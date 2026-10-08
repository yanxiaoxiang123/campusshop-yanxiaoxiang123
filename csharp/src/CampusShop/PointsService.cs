namespace CampusShop;

/// <summary>积分抵扣的结果。</summary>
public record RedeemResult(bool Success, decimal Deduction, int PointsUsed, string Message = "");

/// <summary>
/// 积分。见 docs/requirements.md 第 5 节（新功能，尚未实现）。
/// </summary>
public class PointsService
{
    private readonly Dictionary<string, int> _balance = new();

    /// <summary>查询积分余额。</summary>
    public int GetBalance(string user) => _balance.GetValueOrDefault(user);

    /// <summary>设置积分余额（初始化数据、测试用）。</summary>
    public void SetBalance(string user, int points) => _balance[user] = points;

    /// <summary>订单完成后发放积分，返回本次获得的积分。</summary>
    public int Earn(string user, decimal paid) =>
        throw new NotImplementedException("PointsService.Earn");

    /// <summary>用积分抵扣订单金额。</summary>
    public RedeemResult Redeem(string user, decimal orderAmount) =>
        throw new NotImplementedException("PointsService.Redeem");

    /// <summary>退货：处理该订单获得的积分 earned 和使用的积分 used。</summary>
    public void Return(string user, int earned, int used) =>
        throw new NotImplementedException("PointsService.Return");
}
