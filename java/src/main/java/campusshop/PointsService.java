package campusshop;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 积分。见 docs/requirements.md 第 5 节（新功能，尚未实现）。
 */
public class PointsService {

    private final Map<String, Integer> balance = new HashMap<>();

    /** 查询积分余额。 */
    public int getBalance(String user) {
        return balance.getOrDefault(user, 0);
    }

    /** 设置积分余额（初始化数据、测试用）。 */
    public void setBalance(String user, int points) {
        balance.put(user, points);
    }

    /** 订单完成后发放积分，返回本次获得的积分。 */
    public int earn(String user, BigDecimal paid) {
        throw new UnsupportedOperationException("PointsService.earn 尚未实现");
    }

    /** 用积分抵扣订单金额。 */
    public RedeemResult redeem(String user, BigDecimal orderAmount) {
        throw new UnsupportedOperationException("PointsService.redeem 尚未实现");
    }

    /** 退货：处理该订单获得的积分 earned 和使用的积分 used。 */
    public void returnOrder(String user, int earned, int used) {
        throw new UnsupportedOperationException("PointsService.returnOrder 尚未实现");
    }
}
