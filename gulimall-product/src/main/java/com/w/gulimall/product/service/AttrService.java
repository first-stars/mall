package com.w.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.product.entity.AttrEntity;
import com.w.gulimall.product.vo.AttVo;
import com.w.gulimall.product.vo.AttrRespVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveEntity(AttVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils getNoRelation(Map<String, Object> params, Long attrgroupId);

    /**
     * 在指定的所有属性集合中，挑出检索属性
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

