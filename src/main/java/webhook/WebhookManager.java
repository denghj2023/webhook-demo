package webhook;

/**
 * Webhook Manager
 */
public interface WebhookManager {

    /**
     * 添加消息
     *
     * @param request {@link WebhookVO}
     */
    void addMsg(WebhookVO request);

    /**
     * 配置回调处理
     *
     * @param callbackProcessor 回调处理
     */
    void configCallbackProcessor(CallbackProcessor callbackProcessor);
}
