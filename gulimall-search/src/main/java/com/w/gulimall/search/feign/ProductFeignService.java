package com.w.gulimall.search.feign;

import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author xin
 * @date 2023-02-05-19:05
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/attr/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId);

    @RequestMapping("/product/brand/infos")
    public R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);
}
