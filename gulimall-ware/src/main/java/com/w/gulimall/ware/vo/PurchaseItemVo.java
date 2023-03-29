package com.w.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xin
 * @date 2023-01-18-12:23
 */
@Data
public class PurchaseItemVo {
//    {itemId:1,status:4,reason:""}
    private Long itemId;
    private Integer status;
    private String reason;
}
