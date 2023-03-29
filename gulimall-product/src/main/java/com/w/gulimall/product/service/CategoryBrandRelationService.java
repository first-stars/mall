package com.w.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.product.entity.BrandEntity;
import com.w.gulimall.product.entity.CategoryBrandRelationEntity;
import com.w.gulimall.product.vo.brandVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    List<brandVo> getBrandVoList(List<BrandEntity> brandEntityList);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

