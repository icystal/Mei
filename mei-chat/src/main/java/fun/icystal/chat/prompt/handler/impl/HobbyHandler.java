package fun.icystal.chat.prompt.handler.impl;

import fun.icystal.chat.prompt.handler.SystemPromptHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "prompt.system.hobby.enable", havingValue = "true")
@Order(20)
public class HobbyHandler implements SystemPromptHandler {

    @Value("${prompt.system.hobby.content}")
    private String defaultPrompt;

    @Override
    public String promptKey() {
        return "[爱好]";
    }

    @Override
    public String prompt() {
        return defaultPrompt;
    }
}
