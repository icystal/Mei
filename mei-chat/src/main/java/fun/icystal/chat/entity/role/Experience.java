package fun.icystal.chat.entity.role;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Experience {

    /**
     * 经历发生时的年龄
     * 列表内如果有两个元素, 分别是[起始年龄, 结束年龄]
     */
    private List<Integer> age = new ArrayList<>();

    /**
     * 经历发生的时间
     * 列表内如果有两个元素, 分别是[起始时间, 结束时间]
     */
    private List<LocalDateTime> time = new ArrayList<>();

    /**
     * 经历发生的地点
     */
    private String location;

    /**
     * 经历详情
     */
    private String description;


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (age.size() == 1) {
            sb.append("年龄:").append(age.getFirst()).append(" ");
        } else if (age.size() == 2) {
            sb.append("年龄:").append(age.getFirst()).append("~").append(age.get(1)).append(" ");
        }

        if (time.size() == 1) {
            sb.append("时间:").append(time.getFirst()).append(" ");
        }  else if (time.size() == 2) {
            sb.append("时间:").append(time.getFirst()).append("~").append(time.get(1)).append(" ");
        }

        if(StringUtils.isNotBlank(location)){
            sb.append("地点:").append(location).append(" ");
        }

        sb.append("经历:").append(description).append(" ");

        return sb.toString();
    }

}
