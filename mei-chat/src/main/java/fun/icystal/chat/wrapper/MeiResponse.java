package fun.icystal.chat.wrapper;

import fun.icystal.chat.constant.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MeiResponse<T> {

    private String code;

    private String message;

    private T body;


    public static <T> MeiResponse<T> success(T data) {
        MeiResponse<T> response = new MeiResponse<>();
        response.setBody(data);
        response.setMessage("成功");
        response.setCode(ResponseCode.OK.getCode());
        return response;
    }
}
