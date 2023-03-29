package com.w.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.w.gulimall.product.entity.ProductAttrValueEntity;
import com.w.gulimall.product.service.ProductAttrValueService;
import com.w.gulimall.product.vo.AttVo;
import com.w.gulimall.product.vo.AttrRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.w.gulimall.product.service.AttrService;
import com.w.common.utils.PageUtils;
import com.w.common.utils.R;



/**
 * 商品属性
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;


    @Autowired
    private ProductAttrValueService ProductAttrValueService;


//    /product/attr/base/listforspu/{spuId}
//    获取spu规格
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities = ProductAttrValueService.baseAttrlistforspu(spuId);
        return R.ok().put("data",entities);
    }

//    /product/attr/base/list/{catelogId}
    @GetMapping("/base/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("catelogId") Long catelogId){
        params.put("type","1");
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId);
        return R.ok().put("page",page);
    }

    /**
     * 获取分类销售属性
     * /product/attr/sale/list/{catelogId}
     * @param params
     * @param catelogId
     * @return
     */
    @GetMapping("/sale/list/{catelogId}")
    public R saleAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("catelogId") Long catelogId){
        params.put("type","0");
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId);
        return R.ok().put("page",page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
        AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttVo attr){
		attrService.saveEntity(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttVo attr){
		attrService.updateAttr(attr);
        return R.ok();
    }

//    /product/attr/update/{spuId}
//    修改商品规格
    @PostMapping("/update/{spuId}")
    public R updateSpuattr(@PathVariable("spuId") Long spuId ,
                           @RequestBody List<ProductAttrValueEntity> entities){
        ProductAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();

    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
