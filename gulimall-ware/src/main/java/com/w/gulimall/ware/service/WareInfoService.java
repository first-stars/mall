package com.w.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.ware.entity.WareInfoEntity;
import com.w.gulimall.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 12:07:52
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据收获地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

