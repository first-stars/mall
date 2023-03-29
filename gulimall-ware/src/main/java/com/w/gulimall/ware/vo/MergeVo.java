package com.w.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xin
 * @date 2023-01-17-16:20
 */
@Data
public class MergeVo {
    private Long purchaseId;    //整单id

    private List<Long> items;   //合并项集合

}
