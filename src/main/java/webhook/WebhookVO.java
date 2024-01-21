package webhook;

import java.util.Map;

/**
 * VO对象实现WebhookVO接口，表示支持Webhook回传
 */
public interface WebhookVO {

    /**
     * 设置Webhook回传地址
     *
     * @param url 回传地址
     */
    void setWebhookUrl(String url);

    /**
     * 获取Webhook回传地址
     *
     * @return 回传地址
     */
    String getWebhookUrl();

    /**
     * 设置Webhook回传参数
     *
     * @param params 回传参数
     */
    void setParams(Map<String, Object> params);

    /**
     * 获取Webhook回传参数
     *
     * @return 回传参数
     */
    Map<String, Object> getParams();
}
