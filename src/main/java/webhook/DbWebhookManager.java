package webhook;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 基于DB的Webhook Manager
 */
@Slf4j
public class DbWebhookManager implements WebhookManager {

    @Resource
    private CallbackProcessor callbackProcessor;
    private final Map<Integer, WebHookMsgPO> db = new ConcurrentHashMap<>();

    // 伪代码：保存msg到DB
    @Override
    public void addMsg(WebhookVO request) {
        WebHookMsgPO msg = new WebHookMsgPO();
        msg.setUrl(request.getWebhookUrl());
        msg.setParams(request.getParams() != null ? JSON.toJSONString(request.getParams()) : null);
        msg.setNextCallbackAt(LocalDateTime.now().plusSeconds(10));
        msg.setCallbackCount(0);
        msg.setCallbackSuccess(false);

        msg.setMsgId(RandomUtils.nextInt(0, 100000000));
        db.put(msg.getMsgId(), msg);
        log.info("addMsg msg: {}", msg);
    }

    @Override
    public void configCallbackProcessor(CallbackProcessor callbackProcessor) {
        this.callbackProcessor = callbackProcessor;
    }

    /**
     * 循环查询DB中的MSG，如果满足条件则回调
     */
    @PostConstruct
    public void queryMsgLoop() {
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    // 查询MSG
                    List<WebHookMsgPO> msgList = this.queryMsg();
                    log.debug("queryMsgLoop msgList: {}", msgList);

                    // 回调
                    for (WebHookMsgPO webHookMsgPO : msgList) {
                        this.tryCallback(webHookMsgPO);
                    }
                } finally {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        log.info("queryMsgLoop started");
    }

    // 尝试回调
    private void tryCallback(WebHookMsgPO msg) {
        try {
            this.callbackProcessor.callback(msg.getUrl(), msg.getParams() != null ?
                    JSON.parseObject(msg.getParams(), Map.class) : null);
            this.callbackSuccess(msg);
            log.info("Callback success, msg: {}", msg);
        } catch (Exception | CallbackFailedException e) {
            this.callbackFailed(msg, e.getMessage());
            log.error("Callback failed, msg: {}", msg, e);
        }
    }

    // 伪代码：回传失败
    private void callbackFailed(WebHookMsgPO msg, String failReason) {
        int callbackCount = msg.getCallbackCount() + 1;
        msg.setCallbackSuccess(false);
        msg.setCallbackCount(callbackCount);
        msg.setFailReason(failReason);
        msg.setNextCallbackAt(LocalDateTime.now().plusMinutes(callbackCount));
        db.put(msg.getMsgId(), msg);
        log.debug("callbackFailed msg: {}", msg);
    }

    // 伪代码：回传成功
    private void callbackSuccess(WebHookMsgPO msg) {
        msg.setCallbackSuccess(true);
        msg.setCallbackCount(msg.getCallbackCount() + 1);
        db.put(msg.getMsgId(), msg);
        log.debug("callbackSuccess msg: {}", msg);
    }

    // 伪代码：查询DB中的MSG
    private List<WebHookMsgPO> queryMsg() {
        return db.values().stream()
                .filter(msg -> msg.getNextCallbackAt().isBefore(LocalDateTime.now())) // nextCallbackAt < now
                .filter(msg -> msg.getCallbackCount() < 10) // callbackCount < n
                .filter(msg -> !msg.isCallbackSuccess()) // callbackSuccess = false
                .collect(Collectors.toList());
    }

    @Data
    static class WebHookMsgPO {

        private Integer msgId;
        private String url;
        private String params;
        private LocalDateTime nextCallbackAt;
        private Integer callbackCount;
        private boolean callbackSuccess;
        private String failReason;
    }
}
