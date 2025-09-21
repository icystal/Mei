package fun.icystal.chat.prompt.handler.impl;

import fun.icystal.chat.prompt.handler.SystemPromptHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "prompt.system.profile.enable", havingValue = "true")
@Order(10)
public class ProfileHandler implements SystemPromptHandler {

    @Value("${prompt.system.profile.content}")
    private String defaultPrompt;

    @Override
    public String promptKey() {
        return "[经历]";
    }

    @Override
    public String prompt() {
        return defaultPrompt;
    }
}
