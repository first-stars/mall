package com.w.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.w.common.valid.AddGroup;
import com.w.common.valid.UpdateGroup;
import com.w.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.w.gulimall.product.entity.BrandEntity;
import com.w.gulimall.product.service.BrandService;
import com.w.common.utils.PageUtils;
import com.w.common.utils.R;


/**
 * 品牌
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }
    @RequestMapping("/infos")
    public R infos(@RequestParam("brandIds") List<Long> brandIds){
		List<BrandEntity> brand = brandService.getBrandIdsById(brandIds);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /*BindingResult result*/ ){

//        if (result.hasErrors()){
//            HashMap<String, String> map = new HashMap<>();
//            //获取错误校验结果
//            result.getFieldErrors().forEach(item -> {
//                //获得错误提示
//                String message = item.getDefaultMessage();
//                //获得错误属性名
//                String field = item.getField();
//                map.put(field,message);
//            });
//            return R.error(400,"提交数据不合法").put("data",map);
//        }else {

            brandService.save(brand);
            return R.ok();
//        }

    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     * @param brand
     * @return
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
