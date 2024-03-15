package webhook.core;

import webhook.dto.WebhookRecordDTO;

import java.util.Map;

/**
 * Webhook Manager
 *
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
public interface WebhookManager {

    /**
     * 添加一个Webhook记录，当完成处理后，使用该Webhook记录通知调用方
     *
     * @param recordDTO Webhook记录
     */
    void addWebhookRecord(WebhookRecordDTO recordDTO);

    /**
     * 获取Webhook记录
     *
     * @param messageId Message ID {@link WebhookRecordDTO#getMessageId()}
     * @return Webhook记录
     */
    WebhookRecordDTO getWebhookRecord(String messageId);

    /**
     * 回调消息
     *
     * @param messageId Message ID {@link WebhookRecordDTO#getMessageId()}
     * @param message   要回传给调用方的数据
     */
    void callbackMessage(String messageId, Map<String, Object> message);
}
