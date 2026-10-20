package campusshop.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * 阶段作业 2 的 API 测试补充：三层断言、鉴权/越权、错误原因。
 *
 * 场景覆盖（七类）：
 * 1. 正常请求：postOrder_valid_threeLayerAssertions / postOrder_valid_secondThreeLayerAssertions
 * 2. 缺少必填字段：login_missingPassword_badRequest
 * 3. 取值非法：OrderApiTest.postOrder_invalidQuantity_badRequest（0/-1/100）
 * 4. 未认证：getOrder_noToken_unauthorized / getOrder_invalidToken_unauthorized
 * 5. 无权限：getOrder_otherUsersOrder_forbidden_noLeak
 * 6. 资源不存在：postOrder_unknownProduct_returnsNotFoundAndReason
 * 7. 业务冲突：postOrder_outOfStock_returnsConflictAndReason
 *
 * 每个测试现场登录获取 Token，不硬编码 Token；订单 ID 使用接口返回值，不依赖执行顺序。
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class Stage2ApiTest {

    @LocalServerPort
    int port;

    // ---- 辅助方法 ----

    private String loginAs(String user) {
        return given().port(port).contentType(ContentType.JSON)
                .body(Map.of("username", user, "password", "123456"))
                .post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    private RequestSpecification withToken(String token) {
        RequestSpecification spec = given().port(port).contentType(ContentType.JSON);
        return token == null ? spec : spec.header("Authorization", "Bearer " + token);
    }

    private Response postOrder(String token, String productId, int qty) {
        return withToken(token)
                .body(Map.of("productId", productId, "quantity", qty))
                .post("/api/orders");
    }

    private Response getOrder(String token, int id) {
        return withToken(token).get("/api/orders/" + id);
    }

    private Response getProducts() {
        return given().port(port).get("/api/products");
    }

    private int stockOf(String productId) {
        return getProducts().then().statusCode(200)
                .extract().path("find { it.id == '" + productId + "' }.stock");
    }

    // ---- 1. 正常请求：两个三层断言 ----

    @Test
    void postOrder_valid_threeLayerAssertions() {
        String token = loginAs("alice");
        int before = stockOf("p4");

        Response resp = postOrder(token, "p4", 1);

        // 第 1 层：状态码
        resp.then().statusCode(201);
        // 第 2 层：响应体（p4 单价 129；普通会员满 100 减 10 → 119.00；1 件运费 10 → 129.00）
        resp.then()
                .body("user", equalTo("alice"))
                .body("total", equalTo(129.00f))
                .body("items[0].productId", equalTo("p4"))
                .body("items[0].quantity", equalTo(1));

        int id = resp.path("id");

        // 第 3 层：副作用——订单可以查到、库存确实扣减
        getOrder(token, id).then().statusCode(200)
                .body("id", equalTo(id))
                .body("total", equalTo(129.00f));
        assertThat(stockOf("p4")).isEqualTo(before - 1);
    }

    @Test
    void postOrder_valid_secondThreeLayerAssertions() {
        String token = loginAs("carol");   // 金卡会员：9 折
        int before = stockOf("p3");

        Response resp = postOrder(token, "p3", 3);

        // 第 1 层：状态码
        resp.then().statusCode(201);
        // 第 2 层：响应体（39.90 × 3 = 119.70；金卡 9 折 = 107.73；满 100 减 10 = 97.73；3 件免运费）
        resp.then()
                .body("user", equalTo("carol"))
                .body("total", equalTo(97.73f))
                .body("items[0].productId", equalTo("p3"))
                .body("items[0].quantity", equalTo(3));

        int id = resp.path("id");

        // 第 3 层：副作用——订单详情一致、库存扣减 3
        getOrder(token, id).then().statusCode(200)
                .body("id", equalTo(id))
                .body("total", equalTo(97.73f));
        assertThat(stockOf("p3")).isEqualTo(before - 3);
    }

    // ---- 2. 缺少必填字段 ----

    @Test
    void login_missingPassword_badRequest() {
        given().port(port).contentType(ContentType.JSON)
                .body(Map.of("username", "alice"))
                .post("/api/auth/login")
                .then().statusCode(400)
                .body("error", containsString("用户名和密码不能为空"));
    }

    // ---- 4. 未认证 ----

    @Test
    void getOrder_noToken_unauthorized() {
        getOrder(null, 1001).then().statusCode(401);
    }

    @Test
    void getOrder_invalidToken_unauthorized() {
        given().port(port)
                .header("Authorization", "Bearer not-a-real-token")
                .get("/api/orders/1001")
                .then().statusCode(401);
    }

    // ---- 5. 无权限（越权）----

    @Test
    void getOrder_otherUsersOrder_forbidden_noLeak() {
        int bobOrderId = postOrder(loginAs("bob"), "p2", 1)
                .then().statusCode(201)
                .extract().path("id");

        String aliceToken = loginAs("alice");

        getOrder(aliceToken, bobOrderId).then()
                .statusCode(anyOf(is(403), is(404)))
                .body("error", anyOf(equalTo("无权查看该订单"), equalTo("订单不存在")))
                .body("address", nullValue())
                .body("phone", nullValue());
    }

    // ---- 6. 资源不存在 ----

    @Test
    void postOrder_unknownProduct_returnsNotFoundAndReason() {
        postOrder(loginAs("alice"), "nope", 1).then()
                .statusCode(404)
                .body("error", equalTo("商品不存在"));
    }

    // ---- 7. 业务冲突 ----

    @Test
    void postOrder_outOfStock_returnsConflictAndReason() {
        // p6 纪念徽章库存为 0
        postOrder(loginAs("alice"), "p6", 1).then()
                .statusCode(409)
                .body("error", equalTo("库存不足"));
    }

    // ---- 额外：登录用户名前后空格 ----

    @Test
    void login_usernameWithSpaces_trimsAndSucceeds() {
        given().port(port).contentType(ContentType.JSON)
                .body(Map.of("username", " alice ", "password", "123456"))
                .post("/api/auth/login")
                .then().statusCode(200)
                .body("token", not(emptyOrNullString()));
    }
}
