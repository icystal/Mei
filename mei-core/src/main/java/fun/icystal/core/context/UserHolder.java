package fun.icystal.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import fun.icystal.core.entity.UserInfo;


public class UserHolder {
    private static final TransmittableThreadLocal<UserInfo> TTL =
            new TransmittableThreadLocal<>();

    public static void set(UserInfo user) {
        TTL.set(user);
    }

    public static UserInfo get() {
        return TTL.get();
    }

    public static void remove() {
        TTL.remove();
    }

    public static String getUserId() {
        UserInfo userInfo = TTL.get();
        if (userInfo != null) {
            return userInfo.getUserId();
        }
        return null;
    }

    public static String getUserToken() {
        UserInfo userInfo = TTL.get();
        if (userInfo != null) {
            return userInfo.getUserToken();
        }
        return null;
    }

    public static String getConversationId() {
        UserInfo userInfo = TTL.get();
        if (userInfo != null) {
            return userInfo.getConversationId();
        }
        return null;
    }
}
