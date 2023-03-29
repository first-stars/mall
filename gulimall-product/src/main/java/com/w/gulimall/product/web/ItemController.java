package com.w.gulimall.product.web;

import com.w.gulimall.product.service.SkuInfoService;
import com.w.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author xin
 * @date 2023-02-06-17:00
 */
@Controller
public class ItemController {


    @Autowired
    private SkuInfoService skuInfoService;


    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVo);
        return "item";

    }
}
