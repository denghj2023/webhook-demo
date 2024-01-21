package webhook;

/**
 * 回传失败异常
 */
public class CallbackFailedException extends Throwable {

    public CallbackFailedException(String message) {
        super(message);
    }
}
