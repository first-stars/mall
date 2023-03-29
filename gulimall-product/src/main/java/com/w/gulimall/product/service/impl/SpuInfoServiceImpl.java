package com.w.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.w.common.constant.ProductConstant;
import com.w.common.to.SkuHasStockVo;
import com.w.common.to.SkuReductionTo;
import com.w.common.to.SpuBoundTo;
import com.w.common.to.es.SkuEsModel;
import com.w.common.utils.R;
import com.w.gulimall.product.entity.*;
import com.w.gulimall.product.feign.CouponFeignService;
import com.w.gulimall.product.feign.SearchFeignService;
import com.w.gulimall.product.feign.WareFeignService;
import com.w.gulimall.product.service.*;
import com.w.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    @Override
    public SpuInfoEntity getSkuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = getById(spuId);
        return spuInfoEntity;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO 高级部分完善
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu的基本信息 :pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);


        //2、保存spu的描述图片 :pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);


        //3、保存spu的图片集 :pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);


        //4、保存spu的规格参数 :pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((item) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(item.getAttrId());
            AttrEntity byId = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);


        //5、保存spu的积分信息：gulimall_sms -> sms_spu_bounds

        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode()!=0){
            log.error("远程保存SPU积分失败");
        }
        //5、保存当前spu对应的所有sku信息   :

        List<Skus> skus = vo.getSkus();
        if (skus!=null && skus.size()!=0){

            skus.forEach((sku) -> {

                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0l);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5、1、sku的基本信息：pms_sku_info :
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = sku.getImages().stream().map((img) -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5、2、sku的图片信息：pms_sku_images
                skuImagesService.saveBatch(imagesEntities);

                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map((a) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                //5、3、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5、4、sku的优惠、满减等信息：gulimall_sms -> sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku,skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode()!=0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }


            });
        }



    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w -> {
                w.eq("id",key).or().like("spu_name",key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandld = (String) params.get("brandld");
        if (!StringUtils.isEmpty(brandld) && !"0".equalsIgnoreCase(brandld)){
            wrapper.eq("brand_id",brandld);
        }

        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //1.组装需要的数据

        //查出当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        List<Long> skuIds = skuInfoEntities.stream().map(sku -> {
            return sku.getSkuId();
        }).collect(Collectors.toList());

        //TODO 4,查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = productAttrValueEntities.stream().filter(attr -> {
            return idSet.contains(attr.getAttrId());
        }).map(attr -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(attr, attrs);
            return attrs;
        }).collect(Collectors.toList());

        //TODO 1, 发送远程调用，库存系统是否有库存
        R skusHasStock = wareFeignService.getSkusHasStock(skuIds);

        Map<Long, Boolean> stockMap = null;
        try {
            TypeReference<List<SkuHasStockVo>> listTypeReference = new TypeReference<List<SkuHasStockVo>>(){};
            stockMap = skusHasStock.getData(listTypeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("库存服务查询异常{}",e);
        }


        SkuHasStockVo skuHasStockVo = new SkuHasStockVo();


        //封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> uoProducts = skuInfoEntities.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);

            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //设置库存信息
            if (finalStockMap ==null){
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //TODO 2，热度评分0
            esModel.setHotScore(0l);
            //TODO 3，查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            //设置检索属性
            esModel.setAttrs(attrsList);

            return esModel;
        }).collect(Collectors.toList());

        //TODO 5,发送数据，进行保存
        R r = searchFeignService.productStatusUp(uoProducts);

        if (r.getCode()==0){
            //TODO 6.修改当前spu的状态
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setUpdateTime(new Date());
            spuInfoEntity.setPublishStatus(ProductConstant.StatusEnum.NEW_UP.getCode());
            this.updateById(spuInfoEntity);
        }else {
            //TODO 7.重复调用，接口幂等性；重试机制
        }

    }

}