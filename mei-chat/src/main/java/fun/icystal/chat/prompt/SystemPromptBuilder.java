package fun.icystal.chat.prompt;

import fun.icystal.chat.prompt.role.RoleHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemPromptBuilder {

    @Value("${prompt.role}")
    private String rolePrompt;

    private final RoleHolder roleHolder;

    public String systemPrompt() {
        String role = roleHolder.rolePrompt();
        return String.format(rolePrompt, role);
    }
}
