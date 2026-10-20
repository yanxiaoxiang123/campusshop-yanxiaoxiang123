package campusshop.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * 下单与订单查询接口，需求 1.3 与第 4 节。每个测试自己登录、自己准备数据。
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OrderApiTest {

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
        return withToken(token).body(Map.of("productId", productId, "quantity", qty)).post("/api/orders");
    }

    private Response getOrder(String token, int id) {
        return withToken(token).get("/api/orders/" + id);
    }

    private int createOrderAs(String user) {
        return postOrder(loginAs(user), "p2", 1).then().statusCode(201).extract().path("id");
    }

    // ---- 下单 ----

    @Test
    void postOrder_valid_createsOrder() {
        String token = loginAs("alice");

        Response resp = postOrder(token, "p1", 2);

        // 第 1 层：状态码；第 2 层：响应体（alice 是普通会员；50 × 2 = 100 → 满 100 减 10 = 90；2 件运费 10 → 100.00）
        resp.then().statusCode(201)
                .body("total", equalTo(100.00f))
                .body("user", equalTo("alice"));
        // 第 3 层：副作用——能查到这个订单
        int id = resp.path("id");
        getOrder(token, id).then().statusCode(200);
    }

    @Test
    void postOrder_noToken_unauthorized() {
        postOrder(null, "p1", 1).then().statusCode(401);
    }

    @Test
    void postOrder_outOfStock_conflict() {
        // p6 纪念徽章库存为 0
        postOrder(loginAs("alice"), "p6", 1).then().statusCode(409);
    }

    @Test
    void postOrder_unknownProduct_notFound() {
        postOrder(loginAs("alice"), "nope", 1).then().statusCode(404);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 100})
    void postOrder_invalidQuantity_badRequest(int qty) {
        // 需求 4.6：数量必须是 1～99
        postOrder(loginAs("alice"), "p1", qty).then().statusCode(400);
    }

    // ---- 查询订单 ----

    @Test
    void getOrder_ownOrder_ok() {
        getOrder(loginAs("alice"), 1001).then().statusCode(200);
    }

    @Test
    void getOrder_otherUsersOrder_forbidden() {
        int bobOrderId = createOrderAs("bob");
        String aliceToken = loginAs("alice");

        // 需求 4.7：不能查看他人的订单
        getOrder(aliceToken, bobOrderId).then().statusCode(anyOf(is(403), is(404)));
    }

    @Test
    void getOrder_notExist_notFound() {
        getOrder(loginAs("alice"), 999999).then().statusCode(404);
    }

    // ---- 登录 ----

    @Test
    void login_wrongPassword_unauthorized() {
        given().port(port).contentType(ContentType.JSON)
                .body(Map.of("username", "alice", "password", "wrong"))
                .post("/api/auth/login")
                .then().statusCode(401);
    }

    @Test
    void login_missingUsername_badRequest() {
        // 需求 1.3：用户名必填，缺少时返回 400
        given().port(port).contentType(ContentType.JSON)
                .body(Map.of("password", "123456"))
                .post("/api/auth/login")
                .then().statusCode(400);
    }
}
