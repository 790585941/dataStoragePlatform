package com.zyh432.core.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 公用返回对象
 */
// 保证json序列化的时候，如果属性为null，key也就一起消失
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class Data<T> implements Serializable {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态说明文案
     */
    private String message;

    /**
     * 返回承载
     */
    private T data;

    private Data(Integer code) {
        this.code = code;
    }

    private Data(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Data(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isSuccess() {
        return Objects.equals(this.code, ResponseCode.SUCCESS.getCode());
    }

    public static <T> Data<T> success() {
        return new Data<T>(ResponseCode.SUCCESS.getCode());
    }
    public static <T> Data<T> success(String message) {
        return new Data<T>(ResponseCode.SUCCESS.getCode(),message);
    }

    public static <T> Data<T> success(T data) {
        return new Data<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getDesc(), data);
    }

    public static <T> Data<T> fail(){
        return new Data<T>(ResponseCode.ERROR.getCode());
    }
    public static <T> Data<T> fail(String message) {
        return new Data<T>(ResponseCode.ERROR.getCode(),message);
    }
    public static <T> Data<T> fail(Integer code,String message) {
        return new Data<T>(code,message);
    }
    public static <T> Data<T> fail(ResponseCode responseCode) {
        return new Data<T>(responseCode.getCode(),responseCode.getDesc());
    }
}
