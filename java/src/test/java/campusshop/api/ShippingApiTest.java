package campusshop.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * 满 3 件包邮的 API 测试（需求 2.7）。
 * 每个测试先登录并清空 alice 的购物车，避免服务端状态互相影响。
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ShippingApiTest {

    @LocalServerPort
    int port;

    private String token;

    @BeforeEach
    void loginAndClearCart() {
        token = given().port(port).contentType(ContentType.JSON)
                .body(Map.of("username", "alice", "password", "123456"))
                .post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("token");
        for (String productId : List.of("p1", "p2", "p3", "p4", "p5", "p6")) {
            withToken().delete("/api/cart/items/" + productId)
                    .then().statusCode(204);
        }
    }

    private RequestSpecification withToken() {
        return given().port(port).contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token);
    }

    private void addToCart(String productId, int quantity) {
        withToken().body(Map.of("productId", productId, "quantity", quantity))
                .post("/api/cart/items")
                .then().statusCode(204);
    }

    private Response getCart() {
        return withToken().get("/api/cart");
    }

    private Response postOrder(String productId, int quantity) {
        return withToken().body(Map.of("productId", productId, "quantity", quantity))
                .post("/api/orders");
    }

    @Test
    void getCart_twoItems_includesShipping() {
        addToCart("p1", 2);   // 50 × 2 = 100 → 满减 10 = 90；2 件运费 10

        getCart().then().statusCode(200)
                .body("subtotal", equalTo(100.00f))
                .body("shipping", equalTo(10.00f))
                .body("total", equalTo(100.00f));
    }

    @Test
    void getCart_threeItems_freeShipping() {
        addToCart("p1", 2);   // 100
        addToCart("p2", 1);   // +2.50 = 102.50；3 件免运费；满减 10 → 92.50

        getCart().then().statusCode(200)
                .body("subtotal", equalTo(102.50f))
                .body("shipping", equalTo(0.00f))
                .body("total", equalTo(92.50f));
    }

    @Test
    void getCart_empty_zeroShipping() {
        getCart().then().statusCode(200)
                .body("subtotal", equalTo(0.00f))
                .body("shipping", equalTo(0.00f))
                .body("total", equalTo(0.00f));
    }

    @Test
    void postOrder_twoItems_chargesShipping() {
        Response resp = postOrder("p1", 2);   // 100 → 满减 90；运费 10

        resp.then().statusCode(201)
                .body("total", equalTo(100.00f))
                .body("shipping", equalTo(10.00f));

        withToken().get("/api/orders/" + resp.path("id"))
                .then().statusCode(200)
                .body("total", equalTo(100.00f))
                .body("shipping", equalTo(10.00f));
    }

    @Test
    void postOrder_threeItems_freeShipping() {
        postOrder("p1", 3).then().statusCode(201)   // 150 → 满减 10 = 140；3 件免运费
                .body("total", equalTo(140.00f))
                .body("shipping", equalTo(0.00f));
    }
}
