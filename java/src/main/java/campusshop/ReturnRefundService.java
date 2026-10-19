package campusshop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 退货退款。见 docs/requirements.md 第 6 节。
 */
public class ReturnRefundService {

    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");
    private static final int RETURN_WINDOW_DAYS = 7;
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final PaymentGateway gateway;
    private final InventoryRepository inventory;
    private final Clock clock;
    private final Map<Integer, BigDecimal> refundedByOrder = new LinkedHashMap<>();

    public ReturnRefundService(PaymentGateway gateway, InventoryRepository inventory, Clock clock) {
        this.gateway = gateway;
        this.inventory = inventory;
        this.clock = clock;
    }

    /** 对已支付订单申请退款。 */
    public synchronized RefundResult refund(RefundOrder order, BigDecimal amount) {
        if (order == null || order.paid() == null || order.paid().signum() < 0)
            return failure("订单金额无效", ZERO, ZERO);

        BigDecimal alreadyRefunded = refundedByOrder.getOrDefault(order.id(), ZERO);
        BigDecimal remaining = order.paid().subtract(alreadyRefunded).setScale(2, RoundingMode.HALF_UP);
        if (remaining.signum() <= 0)
            return failure("订单已全额退款", alreadyRefunded, ZERO);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return failure("退款金额必须大于 0", alreadyRefunded, remaining);
        BigDecimal refundAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (refundAmount.signum() <= 0)
            return failure("退款金额必须大于 0", alreadyRefunded, remaining);

        if (refundAmount.compareTo(remaining) > 0)
            return failure("退款金额超过可退金额", alreadyRefunded, remaining);

        if (!withinReturnWindow(order))
            return failure("超过退款期限", alreadyRefunded, remaining);

        boolean gatewaySucceeded;
        try {
            gatewaySucceeded = gateway.refund(order.user(), refundAmount);
        } catch (RuntimeException ex) {
            gatewaySucceeded = false;
        }
        if (!gatewaySucceeded)
            return failure("支付网关退款失败", alreadyRefunded, remaining);

        BigDecimal totalRefunded = alreadyRefunded.add(refundAmount).setScale(2, RoundingMode.HALF_UP);
        refundedByOrder.put(order.id(), totalRefunded);
        BigDecimal newRemaining = order.paid().subtract(totalRefunded).setScale(2, RoundingMode.HALF_UP);

        boolean restocked = false;
        if (newRemaining.signum() == 0) {
            restock(order);
            restocked = true;
        }

        String message = restocked ? "全额退款成功，库存已恢复" : "退款成功";
        return new RefundResult(true, refundAmount, totalRefunded, newRemaining, restocked, message);
    }

    private boolean withinReturnWindow(RefundOrder order) {
        if (order.createdAt() == null)
            return false;
        LocalDate today = LocalDate.ofInstant(clock.instant(), BEIJING);
        long days = ChronoUnit.DAYS.between(order.createdAt().toLocalDate(), today);
        return days >= 0 && days <= RETURN_WINDOW_DAYS;
    }

    private void restock(RefundOrder order) {
        Map<String, Integer> quantityByProduct = new LinkedHashMap<>();
        for (CartItem item : order.items())
            quantityByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        quantityByProduct.forEach(inventory::restore);
    }

    private static RefundResult failure(String message, BigDecimal totalRefunded, BigDecimal remaining) {
        return new RefundResult(false, ZERO, totalRefunded, remaining, false, message);
    }
}
