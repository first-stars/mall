package com.w.gulimall.coupon.service.impl;

import com.w.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.w.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.coupon.dao.SeckillSessionDao;
import com.w.gulimall.coupon.entity.SeckillSessionEntity;
import com.w.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
//        计算最近三天
        List<SeckillSessionEntity> seckillSessionEntities = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));

        if (seckillSessionEntities != null && seckillSessionEntities.size() > 0) {
            List<SeckillSessionEntity> collect = seckillSessionEntities.stream().map(session -> {
                Long id = session.getId();
                //查出sms_seckill_sku_relation表中关联的skuId
                List<SeckillSkuRelationEntity> relationSkus = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationSkus(relationSkus);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 开始时间
     *
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    /**
     * 结束时间
     *
     * @return
     */
    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime of = LocalDateTime.of(plus, max);
        String format = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}