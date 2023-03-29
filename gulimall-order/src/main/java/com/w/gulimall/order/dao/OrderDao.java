package com.w.gulimall.order.dao;

import com.w.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:58:40
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
