package com.w.gulimall.product.exception;

import com.w.common.expection.BizCodeEnume;
import com.w.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * 集中处理所有异常
 * @author xin
 * @date 2023-01-05-13:40
 */
@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.w.gulimall.product.controller")
@RestControllerAdvice(basePackages = "com.w.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型：{}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((item) ->{
            errorMap.put(item.getField(),item.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VAILD_EXPECTION.getCode(), BizCodeEnume.VAILD_EXPECTION.getMsg()).put("data",errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handlerExpection(Throwable throwable){
        log.error("错误：",throwable);
        return R.error(BizCodeEnume.UNKNOW_EXPECTION.getCode(), BizCodeEnume.UNKNOW_EXPECTION.getMsg());
    }
}
