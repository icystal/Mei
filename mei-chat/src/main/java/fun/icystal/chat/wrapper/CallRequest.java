package fun.icystal.chat.wrapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CallRequest extends MeiRequest {

    private String query;

}
