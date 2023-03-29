package com.w.gulimall.search.service;

import com.w.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author xin
 * @date 2023-01-31-19:49
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
