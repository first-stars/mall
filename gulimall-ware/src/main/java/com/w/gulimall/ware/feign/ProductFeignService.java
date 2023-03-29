package com.w.gulimall.ware.feign;

import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xin
 * @date 2023-01-18-14:07
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     *
     * @param skuId
     * @return
     */
    @RequestMapping("product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
