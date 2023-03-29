package com.w.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author xin
 * @date 2023-01-14-20:44
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}
