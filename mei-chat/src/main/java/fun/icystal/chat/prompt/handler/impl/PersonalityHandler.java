package fun.icystal.chat.prompt.handler.impl;

import fun.icystal.chat.prompt.handler.SystemPromptHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "prompt.system.personality.enable", havingValue = "true")
@Order(30)
public class PersonalityHandler implements SystemPromptHandler {

    @Value("${prompt.system.personality.content}")
    private String defaultPrompt;

    @Override
    public String promptKey() {
        return "[性格]";
    }

    @Override
    public String prompt() {
        return defaultPrompt;
    }
}
