package com.w.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:35:05
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

