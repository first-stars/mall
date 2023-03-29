package com.w.gulimall.product.web;

import com.w.gulimall.product.entity.CategoryEntity;
import com.w.gulimall.product.service.CategoryService;
import com.w.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author xin
 * @date 2023-02-01-14:21
 */
@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getLeavel1Categorys();
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){

        Map<String, List<Catelog2Vo>> catalogJson =categoryService.getCatalogJson();
        return catalogJson;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        RLock lock = redissonClient.getLock("my-lock");
        //加锁
        lock.lock();
        //加锁以后10秒中自动解锁
//        lock.lock(10, TimeUnit.MINUTES);
        try {
            System.out.println("加锁成功"+Thread.currentThread().getId());
            Thread.sleep(30000);

        }catch (InterruptedException e) {
        }finally {
            System.out.println("解锁"+Thread.currentThread().getId());

            //3.解锁
            lock.unlock();
        }
        return "hello";
    }

    /**
     * 保证一定能读到最新数据，修改期间，写锁是一个排它锁（互斥锁、独享锁），读锁是一个共享锁
     * 写锁没释放读锁必须等待
     * 读 + 读 ：相当于无锁，并发读，只会在Redis中记录好，所有当前的读锁。他们都会同时加锁成功
     * 写 + 读 ：必须等待写锁释放
     * 写 + 写 ：阻塞方式
     * 读 + 写 ：有读锁。写也需要等待
     * 只要有读或者写的存都必须等待
     * @return
     */
    @GetMapping(value = "/write")
    @ResponseBody
    public String writeValue() {
        String s = "";
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.writeLock();
        try {
            //1、改数据加写锁，读数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            ops.set("writeValue",s);
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

        return s;
    }

    @GetMapping(value = "/read")
    @ResponseBody
    public String readValue() {
        String s = "";
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        //加读锁
        RLock rLock = readWriteLock.readLock();
        try {
            rLock.lock();
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            s = ops.get("writeValue");
            try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

        return s;
    }

    /**
     * 放假、锁门
     * 1班没人了
     * 5个班，全部走完，我们才可以锁大门
     * 分布式闭锁
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();   //等待闭锁都完成
        return "放假了";
    }
    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();   //计数减一

        return id+"班走完了";
    }


    /**
     * 车位停车
     * 3车位
     * 信号量也可以作用分布式流
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.acquire(); //获取信号量，获取一个值，占一个车位
        boolean b = park.tryAcquire();
        return "ok"+b;
    }
    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(); //释放一个车位
        return "ok";
    }


}
