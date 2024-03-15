package example;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import webhook.core.CallbackProcessor;
import webhook.dto.WebhookRecordDTO;
import webhook.exception.CallbackFailedException;
import webhook.support.DbWebhookManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class Client {

    private static final DbWebhookManager webhookManager;

    static {
        // 创建回调处理器
        CallbackProcessor callbackProcessor = (url, params) -> {
            log.info("curl -X POST -H 'Content-Type: application/json' -d '{}' {}", JSON.toJSONString(params), url);
            try (HttpResponse response = HttpUtil.createPost(url).form(params).execute()) {
                if (response.getStatus() != 200) {
                    throw new CallbackFailedException("callback failed, status: " + response.getStatus());
                }
            }
        };

        // 创建WebhookManager
        webhookManager = new DbWebhookManager(callbackProcessor);
        webhookManager.init();
    }

    public static void main(String[] args) {
        // 客户端提交请求时，添加Webhook记录
        WebhookRecordDTO wr = new WebhookRecordDTO();
        wr.setWebhookUrl("http://localhost:8080/webhook");
        wr.setMessageId(UUID.randomUUID().toString());
        webhookManager.addWebhookRecord(wr);

        // ... 其他业务逻辑

        // 请求处理完成后，回调
        Map<String, Object> message = new HashMap<>();
        message.put("result", "success");
        message.put("createAt", System.currentTimeMillis());
        message.put("endAt", System.currentTimeMillis());
        // ...

        webhookManager.callbackMessage(wr.getMessageId(), message);
    }
}
