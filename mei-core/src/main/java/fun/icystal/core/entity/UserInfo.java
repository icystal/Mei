package fun.icystal.core.entity;

import lombok.Data;

@Data
public class UserInfo {

    private String userId;

    private String userToken;

    private String conversationId;
}
