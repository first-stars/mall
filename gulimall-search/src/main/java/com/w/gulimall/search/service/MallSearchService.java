package com.w.gulimall.search.service;

import com.w.gulimall.search.vo.SearchParam;
import com.w.gulimall.search.vo.SearchResult;

/**
 * @author xin
 * @date 2023-02-04-13:39
 */
public interface MallSearchService {
    SearchResult seach(SearchParam param);
}
