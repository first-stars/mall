package com.w.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.product.entity.AttrGroupEntity;
import com.w.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.w.gulimall.product.vo.SpuItemAttrGroupVo;
import com.w.gulimall.product.vo.attrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    void deleteRelation(attrGroupRelationVo[] vos);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

