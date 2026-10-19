package campusshop.e2e;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * E2E 数据准备：通过 API 清空指定用户的购物车。
 * 购物车保存在服务端，如果不清空，上一次运行留下的商品会让总价断言不稳定。
 */
final class TestData {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    private TestData() {
    }

    static void clearCart(String username) throws Exception {
        String loginBody = "{\"username\":\"" + username + "\",\"password\":\"123456\"}";
        HttpResponse<String> login = CLIENT.send(
                HttpRequest.newBuilder(URI.create(BASE_URL + "/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        Matcher matcher = TOKEN.matcher(login.body());
        if (login.statusCode() != 200 || !matcher.find()) {
            throw new IllegalStateException(
                    "清空购物车前登录失败：" + login.statusCode() + " " + login.body());
        }

        for (String productId : List.of("p1", "p2", "p3", "p4", "p5", "p6")) {
            CLIENT.send(
                    HttpRequest.newBuilder(URI.create(BASE_URL + "/api/cart/items/" + productId))
                            .header("Authorization", "Bearer " + matcher.group(1))
                            .DELETE()
                            .build(),
                    HttpResponse.BodyHandlers.discarding());
        }
    }
}
