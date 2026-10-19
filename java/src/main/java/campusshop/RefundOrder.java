package campusshop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 可退款的已支付订单。 */
public record RefundOrder(int id, String user, List<CartItem> items,
                          BigDecimal paid, LocalDateTime createdAt) {
}
