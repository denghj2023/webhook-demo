package example;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import webhook.core.CallbackProcessor;
import webhook.core.WebhookMessageManager;
import webhook.core.WebhookRecordManager;
import webhook.dto.WebhookMessageDTO;
import webhook.dto.WebhookRecordDTO;
import webhook.exception.CallbackFailedException;
import webhook.support.DbWebhookManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class Client {

    private static final DbWebhookManager webhookManager;
    private static final Map<String, WebhookRecordDTO> webhookRecordMap = new ConcurrentHashMap<>();
    private static final Map<String, WebhookMessageDTO> webhookMessageMap = new ConcurrentHashMap<>();

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

        // 创建WebhookRecordManager
        WebhookRecordManager wrManager = new WebhookRecordManager() {
            @Override
            public void addWebhookRecord(WebhookRecordDTO wr) {
                // webhookRecordMap.put(wr.getMessageId(), wr);
            }

            @Override
            public void removeWebhookRecord(String messageId) {
                // webhookRecordMap.remove(messageId);
            }

            @Override
            public WebhookRecordDTO getWebhookRecord(String messageId) {
                WebhookRecordDTO wr = new WebhookRecordDTO();
                wr.setWebhookUrl("http://localhost:8080/webhook");
                wr.setMessageId(UUID.randomUUID().toString());
                return wr;
            }
        };

        // 创建WebhookMessageManager
        WebhookMessageManager wmManager = new WebhookMessageManager() {
            @Override
            public void addWebhookMessage(WebhookMessageDTO wm) {
                webhookMessageMap.put(wm.getMessageId(), wm);
            }

            @Override
            public void removeWebhookMessage(String messageId) {
                webhookMessageMap.remove(messageId);
            }

            @Override
            public List<WebhookMessageDTO> fetchReadyWebhookMessage(int i) {
                return webhookMessageMap.values().stream()
                        .filter(wm -> wm.getNextCallbackTime().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            }

            @Override
            public void callbackFailed(String messageId, String failReason,
                                       int alreadyCallbackTimes, LocalDateTime nextCallbackTime) {
                WebhookMessageDTO wm = webhookMessageMap.get(messageId);
                Objects.requireNonNull(wm, "WebhookMessage not found");
                wm.setFailReason(failReason);
                wm.setAlreadyCallbackTimes(alreadyCallbackTimes);
                wm.setNextCallbackTime(nextCallbackTime);
                webhookMessageMap.put(messageId, wm);
            }
        };

        // 创建WebhookManager
        webhookManager = new DbWebhookManager(callbackProcessor, wrManager, wmManager);
        webhookManager.init();
    }

    public static void main(String[] args) {
//        // 客户端提交请求时，添加Webhook记录
//        WebhookRecordDTO wr = new WebhookRecordDTO();
//        wr.setWebhookUrl("http://localhost:8080/webhook");
//        wr.setMessageId(UUID.randomUUID().toString());
//        webhookManager.addWebhookRecord(wr);

        // ... 其他业务逻辑

        // 请求处理完成后，回调
        Map<String, Object> message = new HashMap<>();
        message.put("result", "success");
        message.put("createAt", System.currentTimeMillis());
        message.put("endAt", System.currentTimeMillis());
        // ...

        webhookManager.callbackMessage(UUID.randomUUID().toString(), message);
    }
}
