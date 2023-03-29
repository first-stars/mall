package com.w.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.w.gulimall.product.vo.attrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<attrGroupRelationVo> vos);
}

