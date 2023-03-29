package com.w.gulimall.product.feign;

import com.w.common.to.es.SkuEsModel;
import com.w.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author xin
 * @date 2023-01-31-20:19
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    /**
     * 上架商品
     * @param skuEsModels
     * @return
     */
    @PostMapping(value = "/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
