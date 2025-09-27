package fun.icystal.chat.prompt;

import fun.icystal.chat.prompt.handler.SystemPromptHandler;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemPromptService {

    @Resource
    private List<SystemPromptHandler> handlers;

    @Value("${prompt.system.title}")
    private String systemTitle;

    @Value("${prompt.system.exhort}")
    private String systemExhort;

    public SystemMessage prompt() {

        StringBuilder sb = new StringBuilder();
        sb.append(systemTitle).append("\n");

        for (SystemPromptHandler handler : handlers) {
            String key = handler.promptKey();
            String prompt = handler.prompt();
            sb.append(key).append("\t").append(prompt).append("\n");
        }

        sb.append(systemExhort).append("\n");

        return new SystemMessage(sb.toString());
    }


}
