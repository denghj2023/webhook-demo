package webhook.support;

import lombok.extern.slf4j.Slf4j;
import webhook.core.CallbackProcessor;
import webhook.core.WebhookManager;
import webhook.core.WebhookMessageManager;
import webhook.core.WebhookRecordManager;
import webhook.dto.WebhookMessageDTO;
import webhook.dto.WebhookRecordDTO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Webhook service based on memory
 *
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
@Slf4j
public class DbWebhookManager implements WebhookManager {

    /**
     * Webhook记录管理器
     */
    @Resource
    private WebhookRecordManager webhookRecordManager;
    /**
     * Webhook消息管理器
     */
    @Resource
    private WebhookMessageManager webhookMessageManager;
    /**
     * 回调处理器
     */
    @Resource
    private CallbackProcessor callbackProcessor;
    /**
     * 回调处理线程池
     */
    @Resource
    private ExecutorService callbackProcessorExecutor;

    @PostConstruct
    public void init() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        callbackProcessorExecutor = new ThreadPoolExecutor(corePoolSize, corePoolSize,
                10L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(corePoolSize * 2),
                new ThreadPoolExecutor.CallerRunsPolicy());

        Executors.newSingleThreadExecutor().execute(this::continueCallbackMessage);
    }

    public DbWebhookManager() {

    }

    public DbWebhookManager(CallbackProcessor callbackProcessor,
                            WebhookRecordManager wrManager,
                            WebhookMessageManager wmManager) {
        this.callbackProcessor = callbackProcessor;
        this.webhookRecordManager = wrManager;
        this.webhookMessageManager = wmManager;
    }

    @Override
    public void addWebhookRecord(WebhookRecordDTO wr) {
        webhookRecordManager.addWebhookRecord(wr);
    }

    @Override
    public WebhookRecordDTO getWebhookRecord(String messageId) {
        return webhookRecordManager.getWebhookRecord(messageId);
    }

    @Override
    public void callbackMessage(String messageId, Map<String, Object> message) {
        WebhookRecordDTO wr = webhookRecordManager.getWebhookRecord(messageId);
        Objects.requireNonNull(wr, "WebhookRecord not found");

        // 保存消息
        WebhookMessageDTO wm = new WebhookMessageDTO();
        wm.setNextCallbackTime(LocalDateTime.now());
        wm.setAlreadyCallbackTimes(0);
        wm.setCallbackData(message);
        wm.setCallbackSuccess(false);
        wm.setFailReason(null);
        wm.setMessageId(messageId);
        wm.setWebhookUrl(wr.getWebhookUrl());

        webhookMessageManager.addWebhookMessage(wm);
    }

    // 持续回调消息
    private void continueCallbackMessage() {
        while (true) {
            try {
                // 获取需要回调的消息
                List<WebhookMessageDTO> wms = webhookMessageManager.fetchReadyWebhookMessage(10);

                // 回调
                wms.forEach(wm -> callbackProcessorExecutor.execute(() -> this.performCallback(wm)));
            } catch (Exception e) {
                log.error("Callback message failed");
            } finally {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Callback message thread interrupted", e);
                }
            }
        }
    }

    // 执行回调
    private void performCallback(WebhookMessageDTO wm) {
        log.debug("Perform callback: {}", wm);
        try {
            // 回调
            callbackProcessor.callback(wm.getWebhookUrl(), wm.getCallbackData());
            wm.setCallbackSuccess(true);

            webhookMessageManager.removeWebhookMessage(wm.getMessageId());
        } catch (Exception e) {
            log.error("Callback message failed");
            int alreadyCallbackTimes = wm.getAlreadyCallbackTimes() + 1;

            LocalDateTime nextCallbackTime = this.getNextExecuteTime(alreadyCallbackTimes);
            if (nextCallbackTime == null) {
                log.error("Callback message failed, no more retry");
                return;
            }

            webhookMessageManager.callbackFailed(wm.getMessageId(),
                    e.getMessage(),
                    alreadyCallbackTimes,
                    nextCallbackTime);
        }
    }

    private static final int[] delayLevelsInSeconds = {
            1, 5, 10, 30,           // Level 1, 2, 3, 4
            60, 120, 180, 240, 300, // Level 5, 6, 7, 8, 9
            360, 420, 480, 540, 600,// Level 10, 11, 12, 13, 14
            1200, 1800,             // Level 15, 16
            3600, 7200              // Level 17, 18
    };

    /**
     * 根据延时等级获取下一次执行的时间。
     *
     * @param level 延时等级
     * @return 下一次执行时间的时间
     */
    private LocalDateTime getNextExecuteTime(int level) {
        if (level < 1 || level > delayLevelsInSeconds.length) {
            return null;
        }

        // Calculate delay in seconds
        long delayInSec = TimeUnit.SECONDS.toMillis(delayLevelsInSeconds[level - 1]) / 1000;

        // Return the future time in seconds
        return LocalDateTime.now().plusSeconds(delayInSec);
    }
}
