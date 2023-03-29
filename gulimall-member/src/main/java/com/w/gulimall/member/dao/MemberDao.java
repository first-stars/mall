package com.w.gulimall.member.dao;

import com.w.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:35:05
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
