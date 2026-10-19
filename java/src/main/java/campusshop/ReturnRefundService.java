package campusshop;

import java.math.BigDecimal;
import java.time.Clock;

/**
 * 退货退款。见 docs/requirements.md 第 6 节（新功能，尚未实现）。
 */
public class ReturnRefundService {

    private final PaymentGateway gateway;
    private final InventoryRepository inventory;
    private final Clock clock;

    public ReturnRefundService(PaymentGateway gateway, InventoryRepository inventory, Clock clock) {
        this.gateway = gateway;
        this.inventory = inventory;
        this.clock = clock;
    }

    /** 对已支付订单申请退款。 */
    public RefundResult refund(RefundOrder order, BigDecimal amount) {
        throw new UnsupportedOperationException("ReturnRefundService.refund 尚未实现");
    }
}
