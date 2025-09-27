package fun.icystal.chat.entity.role;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 每一个 conversation 对应的一个 角色形象
 */
@Data
public class Role {

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别   1: 男     2: 女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 经历列表
     */
    private List<Experience> experiences = new ArrayList<>();

    /**
     * 爱好列表
     */
    private List<String> hobbies = new ArrayList<>();


}
