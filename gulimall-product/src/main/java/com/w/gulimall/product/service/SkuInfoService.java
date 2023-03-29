package com.w.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.product.entity.SkuInfoEntity;
import com.w.gulimall.product.entity.SpuInfoEntity;
import com.w.gulimall.product.vo.SkuItemVo;
import com.w.gulimall.product.vo.SpuSaveVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByConf(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;

}

