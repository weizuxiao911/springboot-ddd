package top.archaiharness.framework.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> {

    /**
     * 业务错误码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 是否成功
     */
    public Boolean getSuccess() {
        return "0".equals(code);
    }

    public static <T> R<T> ok(T data) {
        return R.<T>builder().code("0").data(data).build();
    }

    public static <T> R<T> fail(String message) {
        return R.<T>builder().code("-1").message(message).build();
    }
    
    public static <T> R<T> fail(String code, String message) {
        return R.<T>builder().code(code).message(message).build();
    }
}
