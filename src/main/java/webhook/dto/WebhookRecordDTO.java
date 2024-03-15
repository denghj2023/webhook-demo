package webhook.dto;

import lombok.Data;

/**
 * Webhook Record
 *
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
@Data
public class WebhookRecordDTO {

    /**
     * Message ID
     */
    private String messageId;
    /**
     * Webhook URL
     */
    private String webhookUrl;
}
