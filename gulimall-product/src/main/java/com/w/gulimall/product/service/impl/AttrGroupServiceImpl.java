package com.w.gulimall.product.service.impl;

import com.w.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.w.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.w.gulimall.product.entity.AttrEntity;
import com.w.gulimall.product.service.AttrService;
import com.w.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.w.gulimall.product.vo.SpuItemAttrGroupVo;
import com.w.gulimall.product.vo.attrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.product.dao.AttrGroupDao;
import com.w.gulimall.product.entity.AttrGroupEntity;
import com.w.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

//        SELECT attr_group_id,icon,catelog_id,sort,descript,attr_group_name FROM pms_attr_group WHERE (catelog_id = ? AND ( (attr_group_id = ? OR attr_group_name LIKE ?) ))
        String key = (String)params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj) -> {
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        if (catelogId==0){
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    new QueryWrapper<AttrGroupEntity>()
            );
            return new PageUtils(page);
        }else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public void deleteRelation(attrGroupRelationVo[] vos) {
        //
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(attrAttrgroupRelationEntities);
    }

    /**
     * 根据分类id查出所有分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {

        //1.查询分组信息
        List<AttrGroupEntity> groupEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2.查询所有属性
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map((item) -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> relationAttr = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttr);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        List<SpuItemAttrGroupVo> spuItemAttrGroupVos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return spuItemAttrGroupVos;
    }


}