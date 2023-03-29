package com.w.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.w.gulimall.product.service.CategoryBrandRelationService;
import com.w.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.product.dao.CategoryDao;
import com.w.gulimall.product.entity.CategoryEntity;
import com.w.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组成父子的树形结构
        //2.1找出所有的一级分类
        List<CategoryEntity> leve1Menus = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChilders(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return leve1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {

        //TODO 1.检查等前菜单的
        baseMapper.deleteBatchIds(asList);

    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有的关联数据
     *
     * @param category
     */
//    @CacheEvict(value = "category", key = "'getLeavel1Categorys'")
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLeavel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category",allEntries = true)   //删除category下的所有
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLeavel1Categorys() {
        System.out.println("getLeavel1Categorys......");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));

        return categoryEntities;
    }

    /**
     * redis分布式锁
     * 从数据库查询并封装
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFreomDbWithRedissonLock() {

        //1.占分布式锁，去1redis占 设置过期时间，必须和加锁是同步的，原子的
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }

        return dataFromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFreomDbWithRedisLock() {

        //1.占分布式锁，去1redis占 设置过期时间，必须和加锁是同步的，原子的
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {

//            redisTemplate.delete("lock");//删除锁

                //获取值1对比，对比成功删除=原子操作 lua脚本
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                //删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);

            }

            return dataFromDb;
        } else {
            //加锁失败，重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            return getCatalogJsonFreomDbWithRedisLock();
        }


    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有一级分类
        List<CategoryEntity> leavel1Categorys = getCategoryEntities(selectList, 0l);

        //2.封装数据
        Map<String, List<Catelog2Vo>> parent_cid = leavel1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类的三级
                    List<CategoryEntity> level3Catelog = getCategoryEntities(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        String string = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson", string, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * synchronized锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFreomDbWithLocalLock() {

        synchronized (this) {


            return getDataFromDb();
        }

    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有一级分类
        List<CategoryEntity> leavel1Categorys = getCategoryEntities(selectList, 0l);

        //2.封装数据
        Map<String, List<Catelog2Vo>> parent_cid = leavel1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类的三级
                    List<CategoryEntity> level3Catelog = getCategoryEntities(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        return parent_cid;
    }

    /**
     * TODO 产生堆外内存溢出
     *
     * @return
     */
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        String json = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(json)) {
            Map<String, List<Catelog2Vo>> catalogJsonFreomDb = getCatalogJsonFreomDbWithRedisLock();

            return catalogJsonFreomDb;
        }
        Map<String, List<Catelog2Vo>> catalogJsonFreomDb = JSON.parseObject(json, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return catalogJsonFreomDb;

    }

    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> selectList, Long parend_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parend_cid).collect(Collectors.toList());
        return collect;
    }


    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1.手机当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChilders(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter((ategoryEntity) -> {
            return ategoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity) -> {
            //1.找到子菜单
            categoryEntity.setChildren(getChilders(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2.菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}