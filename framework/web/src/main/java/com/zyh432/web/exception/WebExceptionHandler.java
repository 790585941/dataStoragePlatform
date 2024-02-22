package com.zyh432.web.exception;

import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.exception.DataStoragePlatformFrameworkException;
import com.zyh432.core.response.Data;
import com.zyh432.core.response.ResponseCode;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class WebExceptionHandler {
    @ExceptionHandler(value = DataStoragePlatformBusinessException.class)
    public Data dataStoragePlatformBusinessExceptionHandler(DataStoragePlatformBusinessException e){
        return Data.fail(e.getCode(), e.getMessage());
    }
    //提取出第一个错误信息，并返回一个自定义的错误响应
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Data methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        ObjectError objectError=e.getBindingResult().getAllErrors().stream().findFirst().get();
        return Data.fail(ResponseCode.ERROR_PARAM.getCode(),objectError.getDefaultMessage());
    }
    //用于捕获在数据验证期间发生的违反约束的异常
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Data constraintViolationExceptionHandler(ConstraintViolationException e){
        ConstraintViolation<?> constraintViolation=e.getConstraintViolations().stream().findFirst().get();
        return Data.fail(ResponseCode.ERROR_PARAM.getCode(),constraintViolation.getMessage());
    }
    //处理HTTP请求时，请求缺少必需的参数
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Data missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e){
        return Data.fail(ResponseCode.ERROR_PARAM);
    }
    //是非法或不合适的
    @ExceptionHandler(value = IllegalStateException.class)
    public Data illegalStateExceptionHandler(IllegalStateException e){
        return Data.fail(ResponseCode.ERROR_PARAM);
    }
    //处理数据绑定时出错
    @ExceptionHandler(value = BindException.class)
    public Data bindExceptionHandler(BindException e){
        FieldError fieldError=e.getBindingResult().getFieldErrors().stream().findFirst().get();
        return Data.fail(ResponseCode.ERROR_PARAM.getCode(),fieldError.getDefaultMessage());
    }

    @ExceptionHandler(value = DataStoragePlatformFrameworkException.class)
    public Data dataStoragePlatformFrameworkExceptionHandler(DataStoragePlatformFrameworkException e){
        return Data.fail(ResponseCode.ERROR.getCode(), e.getMessage());
    }

    //捕获运行时异常并以特定的格式返回错误信息
    @ExceptionHandler(value = RuntimeException.class)
    public Data runtimeExceptionHandler(RuntimeException e){
        return Data.fail(ResponseCode.ERROR.getCode(), e.getMessage());
    }

}
