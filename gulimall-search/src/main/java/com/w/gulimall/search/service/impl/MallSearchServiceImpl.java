package com.w.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.w.common.to.es.SkuEsModel;
import com.w.common.utils.R;
import com.w.gulimall.search.config.GulimallElasticSearchConfig;
import com.w.gulimall.search.constant.EsConstant;
import com.w.gulimall.search.feign.ProductFeignService;
import com.w.gulimall.search.service.MallSearchService;
import com.w.gulimall.search.vo.AttrResponseVo;
import com.w.gulimall.search.vo.BrandVo;
import com.w.gulimall.search.vo.SearchParam;
import com.w.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.fetch.subphase.highlight.Highlighter;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xin
 * @date 2023-02-04-13:40
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult seach(SearchParam param) {

        SearchResult searchResult = null;

        //1 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2 执行检索条件
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMM_OPTIONS);

            //3 分析响应数据封装成我们需要的格式
            searchResult = buildSearchResult(searchResponse, param);
        } catch (IOException e) {

            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 准备检索请求
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        //构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /**
         * 模糊匹配，过滤（按属性，品牌，价格，推荐，分类）
         */
        //构建bool-query
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2.2 brandId
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            for (Long aLong : param.getBrandId()) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", aLong));
            }

//            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", param.getBrandId()));
        }
        //1.2.3 attrs
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            param.getAttrs().forEach(item -> {
                //attrs=1_5寸:8寸&2_16G:8G
                BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();

                //attrs=1_5寸:8寸
                String[] s = item.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");//这个属性检索用的值
                boolQueryBuilder1.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder1.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", boolQueryBuilder1, ScoreMode.None);
                boolQueryBuilder.filter(attrs);
            });
        }
//        1.2.4 hasStock
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2.5 skuPrice
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //skuPrice形式为：1_500或_500或500_
            RangeQueryBuilder skuprice = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                skuprice.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    skuprice.lte(s[1]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    skuprice.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(skuprice);
        }

        //封装所有的查询条件
        searchSourceBuilder.query(boolQueryBuilder);

        /**
         * 排序，分页，高亮
         */
        //排序 形式为sort=hotScore_asc/desc
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            SortOrder sortOrder = "ase".equalsIgnoreCase(s[1]) ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], sortOrder);
        }
        //分页
        searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            searchSourceBuilder.highlighter(highlightBuilder);
        }


        /**
         * 聚合分析
         */
        //1. 按照品牌进行聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        //1.1 品牌的子聚合-品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        //1.2 品牌的子聚合-品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

        //2. 按照分类信息进行聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        //3.按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //2.1 按照属性ID进行聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        //2.1.1 在每个属性ID下，按照属性名进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //2.1.1 在每个属性ID下，按照属性值进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        searchSourceBuilder.aggregation(attr_agg);

        String s = searchSourceBuilder.toString();
        System.out.println("构建的DSL" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     * 模糊匹配，过滤（按照属性、分类、品牌，价格区间，库存），完成排序、分页、高亮,聚合分析功能
     *
     * @param searchResponse
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse, SearchParam param) {

        SearchResult searchResult = new SearchResult();
        //1、返回的所有查询到的商品
        SearchHits hits = searchResponse.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        //遍历所有商品信息
        if (hits != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //判断是否按关键字检索，若是就显示高亮，否则不显示
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    //拿到高亮信息显示标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(skuTitleValue);
                }
                esModels.add(esModel);
            }
        }
        searchResult.setProduct(esModels);

        //2、当前商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        //获取属性信息的聚合
        ParsedNested attrAgg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //2、得到属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //3、得到属性的所有值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> {
                return item.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        searchResult.setAttrs(attrVos);

        //3、当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        //获取到品牌的聚合
        ParsedLongTerms brandAgg = searchResponse.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //1、得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            //2、得到品牌的名字
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            //3、得到品牌的图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);

        //4、当前商品涉及到的所有分类信息
        //获取到分类的聚合
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalogAgg = searchResponse.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }

        searchResult.setCatalogs(catalogVos);

        //===============以上可以从聚合信息中获取====================//
        //5、分页信息-页码
        searchResult.setPageNum(param.getPageNum());
        //5、1分页信息、总记录数
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);

        //5、2分页信息-总页码-计算
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        searchResult.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        //6、构建面包导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1、分析每个attr传过来的查询参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attrs=2_5寸:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R info = productFeignService.info(Long.parseLong(s[0]));
                if (info.getCode() == 0) {
                    AttrResponseVo data = info.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                //拿到所有的查询条件，去掉当前
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());


            searchResult.setNavs(collect);
        }
        //品牌分类
//        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
//            List<SearchResult.NavVo> navs = searchResult.getNavs();
//            SearchResult.NavVo navVo = new SearchResult.NavVo();
//
//            navVo.setNavName("品牌");
//            //远程查询所有品牌
//            R r = productFeignService.brandsInfo(param.getBrandId());
//            if (r.getCode()==0){
//                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
//                });
//                StringBuffer stringBuffer = new StringBuffer();
//                for (BrandVo brandVo : brand) {
//                    stringBuffer.append(brandVo.getBrandName()+";");
//                }
//                navVo.setNavValue(stringBuffer.toString());
//            }
//        }


        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode.replace("+", "%20");  //浏览器对空格的编码和Java不一样，差异化处理
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = param.get_queryString().replace("&"+key+"=" + encode, "");
        return replace;
    }
}
