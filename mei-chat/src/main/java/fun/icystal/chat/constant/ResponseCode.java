package fun.icystal.chat.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    OK("0", "成功"),

    UNKNOWN("-1", "未知异常");

    private final String code;

    private final String desc;
}
