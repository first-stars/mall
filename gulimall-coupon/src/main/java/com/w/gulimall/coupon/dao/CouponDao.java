package com.w.gulimall.coupon.dao;

import com.w.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:20:56
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
