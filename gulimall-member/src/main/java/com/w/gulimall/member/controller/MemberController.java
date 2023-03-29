package com.w.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.w.common.expection.BizCodeEnume;
import com.w.gulimall.member.exception.PhoneException;
import com.w.gulimall.member.exception.UsernameException;
import com.w.gulimall.member.feign.CouponFeignService;
import com.w.gulimall.member.vo.MemberRegistVo;
import com.w.gulimall.member.vo.MemberUserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.w.gulimall.member.entity.MemberEntity;
import com.w.gulimall.member.service.MemberService;
import com.w.common.utils.PageUtils;
import com.w.common.utils.R;


/**
 * 会员
 *
 * @author xin
 * @email 973169778@gmail.com
 * @date 2022-12-28 11:35:05
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    /**
     * 登录
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberUserLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity!=null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());

        }

    }


    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupon = couponFeignService.membercoupon();
        return R.ok().put("member", memberEntity).put("coupon", membercoupon.get("coupon"));

    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.regist(vo);
        } catch (UsernameException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
