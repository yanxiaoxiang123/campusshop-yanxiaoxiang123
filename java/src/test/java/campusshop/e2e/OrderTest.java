package campusshop.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class OrderTest extends E2ETest {

    @BeforeEach
    void resetCarts() throws Exception {
        // 购物车保存在服务端：每个测试独立准备数据，避免上次运行残留影响总价。
        TestData.clearCart("alice");
        TestData.clearCart("bob");
    }

    @Test
    void order_twoNotebooks_showsOrderNumber(Page page) {
        loginAs(page, "alice");
        Locator row = page.getByRole(AriaRole.ROW,
            new Page.GetByRoleOptions().setName("笔记本"));
        row.getByLabel("数量").fill("2");
        row.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("加入购物车")).click();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("购物车")).click();
        // alice 是普通会员：50 × 2 = 100，满 100 减 10
        assertThat(page.getByTestId("cart-total"))
            .hasText("¥90.00");
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("提交订单")).click();
        assertThat(page.getByTestId("order-no"))
            .hasText(Pattern.compile("^CS\\d+$"));
    }

    @Test
    void cart_empty_orderButtonDisabled(Page page) {
        loginAs(page, "bob");   // bob 的购物车为空
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("购物车")).click();

        assertThat(page.getByText("购物车为空"))
            .isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("提交订单"))).isDisabled();
    }
}
