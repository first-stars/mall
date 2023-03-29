package com.w.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xin
 * @date 2023-01-30-21:10
 */
@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMM_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();


        COMM_OPTIONS=builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("114.55.9.31", 9200, "http")));
        return client;
    }
}
