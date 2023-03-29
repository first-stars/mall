package com.w.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.w.gulimall.product.entity.AttrEntity;
import com.w.gulimall.product.service.AttrAttrgroupRelationService;
import com.w.gulimall.product.service.AttrService;
import com.w.gulimall.product.service.CategoryService;
import com.w.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.w.gulimall.product.vo.attrGroupRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.w.gulimall.product.entity.AttrGroupEntity;
import com.w.gulimall.product.service.AttrGroupService;
import com.w.common.utils.PageUtils;
import com.w.common.utils.R;



/**
 * 属性分组
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;


    /**
     * 获取分类下所有分组&关联属性
     * /product/attrgroup/{catelogId}/withattr
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        //1.查出等前分类下的所有属性分组
        //2.查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }

    /**
     * 添加属性与分组关联关系
     * /product/attrgroup/attr/relation
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<attrGroupRelationVo> vos){
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 获取属性分组没有关联的其他属性
     * /product/attrgroup/{attrgroupId}/noattr/relation
     * @param attrgroupId
     * @param params
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId,
                          @RequestParam Map<String,Object> params){
        PageUtils page = attrService.getNoRelation(params,attrgroupId);
        return R.ok().put("page",page);

    }

    /**
     * 获取属性分组的关联的所有属性
     * /product/attrgroup/{attrgroupId}/attr/relation
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entities);

    }


    /**
     * /product/attrgroup/attr/relation/delete
     * 删除属性与分组的关联关系
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody attrGroupRelationVo[] vos){
        attrGroupService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){

//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long path[] = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
