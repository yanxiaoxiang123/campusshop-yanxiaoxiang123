package campusshop.steps;

import campusshop.CartItem;
import campusshop.InventoryRepository;
import campusshop.PaymentGateway;
import campusshop.RefundOrder;
import campusshop.RefundResult;
import campusshop.ReturnRefundService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** “退货退款”场景的步骤定义，直接调用领域层。 */
public class RefundSteps {

    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");
    // 北京时间 2026-10-19 10:00
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-10-19T02:00:00Z"), BEIJING);

    private final StubGateway gateway = new StubGateway();
    private final StubInventory inventory = new StubInventory();
    private final ReturnRefundService service = new ReturnRefundService(gateway, inventory, CLOCK);

    private RefundOrder order;
    private RefundResult result;

    @Given("订单实付金额为 {bigdecimal} 元，创建于 {int} 天前")
    public void orderCreatedDaysAgo(BigDecimal paid, int daysAgo) {
        LocalDate today = LocalDate.ofInstant(CLOCK.instant(), BEIJING);
        order = new RefundOrder(1001, "alice",
                List.of(new CartItem("p1", "笔记本", new BigDecimal("50.00"), 2)),
                paid, today.minusDays(daysAgo).atTime(10, 0));
    }

    @Given("商品 {string} 当前库存为 {int} 件")
    public void stockIs(String productId, int quantity) {
        inventory.setStock(productId, quantity);
    }

    @Given("支付网关不可用")
    public void gatewayIsDown() {
        gateway.success = false;
    }

    @Given("用户已退款 {bigdecimal} 元")
    public void alreadyRefunded(BigDecimal amount) {
        RefundResult r = service.refund(order, amount);
        assertTrue(r.success(), () -> "前置退款失败：" + r.message());
    }

    @When("用户申请退款 {bigdecimal} 元")
    public void applyRefund(BigDecimal amount) {
        result = service.refund(order, amount);
    }

    @Then("退款成功")
    public void refundSucceeded() {
        assertTrue(result.success(), () -> "应退款成功，实际：" + result.message());
    }

    @Then("退款失败，提示 {string}")
    public void refundFailedWith(String message) {
        assertFalse(result.success(), () -> "应退款失败，实际成功，退款 " + result.refunded());
        assertEquals(message, result.message());
    }

    @Then("本次退款 {bigdecimal} 元，累计退款 {bigdecimal} 元，剩余可退 {bigdecimal} 元")
    public void amounts(BigDecimal refunded, BigDecimal totalRefunded, BigDecimal remaining) {
        assertEquals(0, refunded.compareTo(result.refunded()),
                () -> "本次退款应为 " + refunded + "，实际 " + result.refunded());
        assertEquals(0, totalRefunded.compareTo(result.totalRefunded()),
                () -> "累计退款应为 " + totalRefunded + "，实际 " + result.totalRefunded());
        assertEquals(0, remaining.compareTo(result.remaining()),
                () -> "剩余可退应为 " + remaining + "，实际 " + result.remaining());
    }

    @Then("商品 {string} 库存恢复为 {int} 件")
    public void stockRestoredTo(String productId, int expected) {
        assertTrue(result.restocked(), "全额退款后应恢复库存");
        assertEquals(expected, inventory.getStock(productId));
    }

    @Then("商品 {string} 库存保持 {int} 件")
    public void stockUnchanged(String productId, int expected) {
        assertEquals(expected, inventory.getStock(productId));
    }

    /** 支付网关测试替身。 */
    private static final class StubGateway implements PaymentGateway {
        private boolean success = true;

        @Override
        public boolean charge(String userId, BigDecimal amount) {
            return true;
        }

        @Override
        public boolean refund(String userId, BigDecimal amount) {
            return success;
        }
    }

    /** 库存测试替身。 */
    private static final class StubInventory implements InventoryRepository {
        private final Map<String, Integer> stock = new HashMap<>();

        void setStock(String productId, int quantity) {
            stock.put(productId, quantity);
        }

        @Override
        public int getStock(String productId) {
            return stock.getOrDefault(productId, 0);
        }

        @Override
        public void deduct(String productId, int quantity) {
            stock.put(productId, getStock(productId) - quantity);
        }

        @Override
        public void restore(String productId, int quantity) {
            stock.put(productId, getStock(productId) + quantity);
        }
    }
}
