package com.w.gulimall.thirdparty.service;

import com.w.common.utils.R;

/**
 * @author xin
 * @date 2023-02-07-21:05
 */
public interface EmailService {
    R sendSimpleEmail(String to, String subject, String text);
}
