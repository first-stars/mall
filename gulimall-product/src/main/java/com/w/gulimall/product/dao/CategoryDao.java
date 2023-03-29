package com.w.gulimall.product.dao;

import com.w.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
