package example;

import lombok.Data;
import webhook.WebhookVO;

import java.util.Map;

@Data
public class ExampleVO implements WebhookVO {

    private String phoneId;
    private String op;
    private String webhookUrl;
    private Map<String, Object> params;
}
