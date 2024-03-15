package webhook.core;

import webhook.dto.WebhookRecordDTO;

/**
 * Webhook Record Manager
 *
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
public interface WebhookRecordManager {

    void addWebhookRecord(WebhookRecordDTO wr);

    void removeWebhookRecord(String messageId);

    WebhookRecordDTO getWebhookRecord(String messageId);
}
