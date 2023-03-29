package com.w.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xin
 * @date 2023-01-14-11:36
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal GrowBounds;
}
