package com.w.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.w.common.utils.R;
import com.w.gulimall.product.dao.SkuSaleAttrValueDao;
import com.w.gulimall.product.entity.SkuImagesEntity;
import com.w.gulimall.product.entity.SpuInfoDescEntity;
import com.w.gulimall.product.entity.SpuInfoEntity;
import com.w.gulimall.product.feign.SeckillFeignService;
import com.w.gulimall.product.service.*;
import com.w.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.product.dao.SkuInfoDao;
import com.w.gulimall.product.entity.SkuInfoEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByConf(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        /**
         * key:
         * 华为
         * catelogId:
         * 225
         * brandId:
         * 9
         * min:
         * 9999
         * max:
         * 99
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        String max = (String) params.get("max");

        //大于等于
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }

        //小于等于
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    wrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        //1、sku基本信息获取 pms sku info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);

        //3、获取sku的销售属性组合
        CompletableFuture<Void> saleFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueDao.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVos);
        }, threadPoolExecutor);

        //4、获取spu的介绍
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);

        //5、获取spu的规格参数
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVo> spuItemAttrGroupVo = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(spuItemAttrGroupVo);
        }, threadPoolExecutor);

        //2、sku的图片信息
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);

        //查询等前sku是否参与秒杀优惠
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R skuSeckilInfo = seckillFeignService.getSkuSeckilInfo(skuId);
            if (skuSeckilInfo.getCode() == 0) {
                SeckillSkuVo seckillSkuVo = skuSeckilInfo.getData(new TypeReference<SeckillSkuVo>() {
                });
                skuItemVo.setSeckillSkuVo(seckillSkuVo);
            }
        }, threadPoolExecutor);


        //等待所有异步任务都完成
        CompletableFuture.allOf(saleFuture, descFuture, baseAttrFuture, imageFuture,seckillFuture).get();

        return skuItemVo;
    }


}