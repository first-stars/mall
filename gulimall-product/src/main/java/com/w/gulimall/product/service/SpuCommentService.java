package com.w.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-27 19:34:38
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

