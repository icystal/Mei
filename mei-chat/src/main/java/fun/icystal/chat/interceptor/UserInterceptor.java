package fun.icystal.chat.interceptor;

import fun.icystal.core.context.UserHolder;
import fun.icystal.core.entity.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_TOKEN = "X-User-Token";
    private static final String HEADER_CONVERSATION_ID = "X-CONVERSATION-ID";

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        String userId = request.getHeader(HEADER_USER_ID);
        String userToken = request.getHeader(HEADER_USER_TOKEN);
        String conversationId = request.getHeader(HEADER_CONVERSATION_ID);

        if (StringUtils.isAnyBlank(userId, userToken)) {
            log.warn("请求头缺少用户信息，userId: {}，userToken: {}", userId, userToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        UserInfo user = new UserInfo();
        user.setUserId(userId);
        user.setUserToken(userToken);

        if (StringUtils.isBlank(conversationId)) {
            conversationId = UUID.randomUUID().toString();
        }
        user.setConversationId(conversationId);

        UserHolder.set(user);
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        UserHolder.remove();
    }
}
