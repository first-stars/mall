package com.w.gulimall.search.controller;

import com.w.common.expection.BizCodeEnume;
import com.w.common.to.es.SkuEsModel;
import com.w.common.utils.R;
import com.w.gulimall.search.service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author xin
 * @date 2023-01-31-20:10
 */
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 上架商品
     * @param skuEsModels
     * @return
     */
    @PostMapping(value = "/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean status=false;

        try {
            status = productSaveService.productStatusUp(skuEsModels);
        }catch (IOException e){
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(status){
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }else {
            return R.ok();

        }

    }
}
