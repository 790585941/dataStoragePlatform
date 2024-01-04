package com.zyh432.core.exception;

import com.zyh432.core.response.ResponseCode;
import lombok.Data;

/**
 * 自定义全局业务异常类
 */
@Data
public class DataStoragePlatformBusinessException extends RuntimeException{
    /**
     * 错误码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String message;

    public DataStoragePlatformBusinessException(ResponseCode responseCode){
        this.code= responseCode.getCode();
        this.message= responseCode.getDesc();
    }
    public DataStoragePlatformBusinessException(Integer code,String message){
        this.code= code;
        this.message= message;
    }
    public DataStoragePlatformBusinessException(String message){
        this.code= ResponseCode.ERROR_PARAM.getCode();
        this.message= message;
    }
    public DataStoragePlatformBusinessException(){
        this.code= ResponseCode.ERROR_PARAM.getCode();
        this.message= ResponseCode.ERROR_PARAM.getDesc();
    }

}
