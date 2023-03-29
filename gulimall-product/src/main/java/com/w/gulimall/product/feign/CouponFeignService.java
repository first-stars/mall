package com.w.gulimall.product.feign;

import com.w.common.to.SkuReductionTo;
import com.w.common.to.SpuBoundTo;
import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author xin
 * @date 2023-01-14-11:27
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {


    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);



}
