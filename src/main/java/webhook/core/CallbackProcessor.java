package webhook.core;

import java.util.Map;

/**
 * 回调处理器
 */
@FunctionalInterface
public interface CallbackProcessor {

    /**
     * 回调处理
     *
     * @param url    回调地址
     * @param params 回调参数
     */
    void callback(String url, Map<String, Object> params);
}
