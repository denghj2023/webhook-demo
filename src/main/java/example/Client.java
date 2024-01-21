package example;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import webhook.CallbackFailedException;
import webhook.DbWebhookManager;
import webhook.WebhookManager;

import java.util.UUID;

@Slf4j
public class Client {

    private static final WebhookManager webhookManager;

    static {
        webhookManager = new DbWebhookManager();
        webhookManager.configCallbackProcessor((url, params) -> {
            log.info("curl -X POST -H 'Content-Type: application/json' -d '{}' {}", JSON.toJSONString(params), url);
            try (HttpResponse response = HttpUtil.createPost(url).form(params).execute()) {
                if (response.getStatus() != 200) {
                    throw new CallbackFailedException("callback failed, status: " + response.getStatus());
                }
            }
        });
        ((DbWebhookManager) webhookManager).queryMsgLoop();
    }

    public static void main(String[] args) {
        // 客户端请求
        ExampleVO request = new ExampleVO();
        request.setPhoneId(UUID.randomUUID().toString());
        request.setOp("START");
        request.setWebhookUrl("http://localhost:8080/webhook");

        // 处理请求并保存WebhookRequest#url、params
        /*
        do something
         */

        // 处理成功，添加到WebhookManager
        webhookManager.addMsg(request);
    }
}
