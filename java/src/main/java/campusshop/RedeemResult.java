package campusshop;

import java.math.BigDecimal;

/** 积分抵扣的结果。 */
public record RedeemResult(boolean success, BigDecimal deduction, int pointsUsed, String message) {
}
