package webhook.core;

import webhook.dto.WebhookMessageDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Webhook Message Manager
 *
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
public interface WebhookMessageManager {

    void addWebhookMessage(WebhookMessageDTO wm);

    void removeWebhookMessage(String messageId);

    List<WebhookMessageDTO> fetchReadyWebhookMessage(int i);

    void callbackFailed(String messageId,
                        String failReason,
                        int alreadyCallbackTimes,
                        LocalDateTime nextCallbackTime);
}
