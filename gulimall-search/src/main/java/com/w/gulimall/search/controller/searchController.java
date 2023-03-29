package com.w.gulimall.search.controller;

import com.w.gulimall.search.service.MallSearchService;
import com.w.gulimall.search.vo.SearchParam;
import com.w.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xin
 * @date 2023-02-03-20:31
 */
@Controller
public class searchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        param.set_queryString(request.getQueryString());
        //1.根据页面传递的数据，去es中检索商品
        SearchResult seach = mallSearchService.seach(param);
        model.addAttribute("result",seach);
        return "list";
    }

}
