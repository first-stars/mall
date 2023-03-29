package com.w.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.w.gulimall.product.entity.BrandEntity;
import com.w.gulimall.product.entity.CategoryEntity;
import com.w.gulimall.product.service.BrandService;
import com.w.gulimall.product.service.CategoryService;
import com.w.gulimall.product.vo.brandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.product.dao.CategoryBrandRelationDao;
import com.w.gulimall.product.entity.CategoryBrandRelationEntity;
import com.w.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        BrandEntity brand = brandService.getById(brandId);
        categoryBrandRelation.setBrandName(brand.getName());

        Long catelogId = categoryBrandRelation.getCatelogId();
        CategoryEntity category = categoryService.getById(catelogId);
        categoryBrandRelation.setCatelogName(category.getName());

        this.save(categoryBrandRelation);

    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));

    }

    @Override
    public void updateCategory(Long catId, String name) {
//        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
//        categoryBrandRelationEntity.setCatelogName(name);
//        categoryBrandRelationEntity.setCatelogId(catId);
//        this.update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq(""));
        baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<brandVo> getBrandVoList(List<BrandEntity> brandEntityList) {
        List<brandVo> collect = brandEntityList.stream().map((item) -> {
            brandVo brandVo = new brandVo();
            brandVo.setBrandId(item.getBrandId());
            brandVo.setBrandName(item.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> catelogId = baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntity> collect = catelogId.stream().map((item) -> {
            Long brandId = item.getBrandId();
            BrandEntity byId = brandService.getById(brandId);
            return byId;
        }).collect(Collectors.toList());

        return collect;
    }

}