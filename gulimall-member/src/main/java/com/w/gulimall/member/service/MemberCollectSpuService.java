package com.w.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.utils.PageUtils;
import com.w.gulimall.member.entity.MemberCollectSpuEntity;

import java.util.Map;

/**
 * 会员收藏的商品
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:35:05
 */
public interface MemberCollectSpuService extends IService<MemberCollectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

