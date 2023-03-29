package com.w.gulimall.product.vo;

import lombok.Data;

/**
 * @author xin
 * @date 2023-01-09-20:51
 */
@Data
public class AttrRespVo extends AttVo{
//              "catelogName": "手机/数码/手机", //所属分类名字
//            "groupName": "主体", //所属分组名字
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;

}
