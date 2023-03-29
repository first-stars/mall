package com.w.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xin
 * @date 2023-01-18-12:23
 */
@Data
public class PurchaseDoneVo {
    private Long id;
    private List<PurchaseItemVo> items;
}
