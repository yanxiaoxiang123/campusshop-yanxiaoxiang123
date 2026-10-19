package campusshop;

import java.math.BigDecimal;

/** 退货退款结果。 */
public record RefundResult(boolean success, BigDecimal refunded, BigDecimal totalRefunded,
                           BigDecimal remaining, boolean restocked, String message) {
}
