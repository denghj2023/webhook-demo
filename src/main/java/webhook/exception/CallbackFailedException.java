package webhook.exception;

/**
 * @author haijun@superads.cn
 * @create 2024/3/15
 */
public class CallbackFailedException extends RuntimeException {

    public CallbackFailedException(String message) {
        super(message);
    }
}
