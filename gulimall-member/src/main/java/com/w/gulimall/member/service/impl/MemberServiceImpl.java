package com.w.gulimall.member.service.impl;

import com.w.gulimall.member.dao.MemberLevelDao;
import com.w.gulimall.member.entity.MemberLevelEntity;
import com.w.gulimall.member.exception.PhoneException;
import com.w.gulimall.member.exception.UsernameException;
import com.w.gulimall.member.vo.MemberRegistVo;
import com.w.gulimall.member.vo.MemberUserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.member.dao.MemberDao;
import com.w.gulimall.member.entity.MemberEntity;
import com.w.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        checkPhoneUnique(vo.getPhone());
        checkUsernamelUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());

//        密码进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneException();
        }
    }

    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));

        if (memberEntity == null) {
//            登陆失败
            return null;
        } else {
            String pwd = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, pwd);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public void checkUsernamelUnique(String userName) throws UsernameException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UsernameException();
        }
    }
}