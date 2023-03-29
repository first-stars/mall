package com.w.gulimall.member.exception;

/**
 * @author xin
 * @date 2023-02-08-13:20
 */
public class PhoneException extends RuntimeException{
    public PhoneException() {
        super("存在相同的手机号");
    }
}
