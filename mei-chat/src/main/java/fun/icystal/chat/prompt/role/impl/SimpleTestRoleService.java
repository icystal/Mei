package fun.icystal.chat.prompt.role.impl;

import fun.icystal.chat.prompt.role.RoleHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SimpleTestRoleService implements RoleHolder {

    @Value("${role.test}")
    private String role;

    @Override
    public String rolePrompt() {
        return role;
    }
}
