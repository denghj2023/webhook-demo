package webhook.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Webhook消息
 *
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WebhookMessageDTO extends WebhookRecordDTO {

    /**
     * 下次回调时间
     */
    private LocalDateTime nextCallbackTime;
    /**
     * 已回调次数
     */
    private Integer alreadyCallbackTimes;
    /**
     * 回调数据
     */
    private Map<String, Object> callbackData;
    /**
     * 是否回调成功
     */
    private boolean callbackSuccess;
    /**
     * 失败原因
     */
    private String failReason;
}
