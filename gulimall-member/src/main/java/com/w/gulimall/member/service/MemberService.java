package com.w.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.member.entity.MemberEntity;
import com.w.gulimall.member.exception.PhoneException;
import com.w.gulimall.member.exception.UsernameException;
import com.w.gulimall.member.vo.MemberRegistVo;
import com.w.gulimall.member.vo.MemberUserLoginVo;

import java.util.Map;

/**
 * 会员
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:35:05
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkUsernamelUnique(String userName)throws UsernameException;

    void checkPhoneUnique(String phone)throws PhoneException;

    MemberEntity login(MemberUserLoginVo vo);
}

