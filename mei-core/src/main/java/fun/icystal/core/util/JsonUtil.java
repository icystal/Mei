package fun.icystal.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Jackson 封装工具类
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper MAPPER;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    static {
        MAPPER = new ObjectMapper();
        // 统一日期格式
        MAPPER.setDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));
        // 忽略 null 值
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 禁用将 Date 转为时间戳
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略空 Bean 报错
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 忽略未知字段
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Java 8 时间模块注册（支持 LocalDateTime 等）
        JavaTimeModule timeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        MAPPER.registerModule(timeModule);
    }

    /* ===================== 序列化 ===================== */

    public static String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("toJSONString error", e);
            return null;
        }
    }

    /* ===================== 反序列化 ===================== */

    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("parseObject error", e);
            return null;
        }
    }

    /**
     * 复杂泛型反序列化（List<Map<...>> 等）
     * 用法：List<XXX> list = parseObject(json, new TypeReference<List<XXX>>() {})
     */
    public static <T> T parseObject(String json, TypeReference<T> reference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, reference);
        } catch (IOException e) {
            log.error("parseObject with TypeReference error", e);
            return null;
        }
    }

    /**
     * 指定集合元素类型反序列化
     * 用法：List<XXX> list = parseArray(json, XXX.class)
     */
    public static <T> List<T> parseArray(String json, Class<T> elementClazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JavaType type = MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementClazz);
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            log.error("parseArray error", e);
            return null;
        }
    }

    /* ===================== 其他辅助 ===================== */

    public static Map<String, Object> toMap(String json) {
        return parseObject(json, new TypeReference<Map<String, Object>>() {});
    }

    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return MAPPER.convertValue(fromValue, toValueType);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    private JsonUtil() {}
}