package com.w.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.coupon.entity.HomeAdvEntity;

import java.util.Map;

/**
 * 首页轮播广告
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:20:56
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

