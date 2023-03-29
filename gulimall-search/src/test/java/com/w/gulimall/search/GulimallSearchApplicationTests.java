package com.w.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.w.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;
    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Data
    class user{
        private String userName;
        private String userAge;
        private String userGender;
    }

    /**
     * 保存更新
     * @throws IOException
     */
    @Test
    public void indextData() throws IOException {
        IndexRequest users = new IndexRequest("users");
        users.id("1");
        user user = new user();
        user.setUserName("张三");
        user.setUserAge("18");
        user.setUserGender("男");
        String jsonString = JSON.toJSONString(user);

        users.source(jsonString, XContentType.JSON);

        IndexResponse index = client.index(users, GulimallElasticSearchConfig.COMM_OPTIONS);

        System.out.println(index);
    }
    @Test
    public void searchData() throws IOException {
        //1、创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，检索条件
        //SearchSourceBuilder searchSourceBuilder封装的条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(100);
        searchSourceBuilder.aggregation(ageAgg);

        AvgAggregationBuilder balance = AggregationBuilders.avg("ageBalanceAvg").field("balance");
        searchSourceBuilder.aggregation(balance);

        System.out.println("检索条件"+searchSourceBuilder.toString());

        searchRequest.source(searchSourceBuilder);


        //2、执行检索
        SearchResponse search = client.search(searchRequest, GulimallElasticSearchConfig.COMM_OPTIONS);
        //3、分析条件
        System.out.println(search.toString());

        //3.1、获取所有查到的数据
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits){
            String string = hit.getSourceAsString();
            Accout accout = JSON.parseObject(string, Accout.class);
            System.out.println("accout:"+accout);
        }
        System.out.println("----------------");
        //3.2、获取这次检索的分析信息
        Aggregations aggregations = search.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄"+keyAsString+"：人数"+bucket.getDocCount());
        }
        System.out.println("----------------");

        Avg ageBalanceAvg = aggregations.get("ageBalanceAvg");

        System.out.println("平均薪资："+ageBalanceAvg.getValue());

    }


    static class Accout {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
        public void setAccount_number(int account_number) {
            this.account_number = account_number;
        }
        public int getAccount_number() {
            return account_number;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }
        public int getBalance() {
            return balance;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }
        public String getFirstname() {
            return firstname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
        public String getLastname() {
            return lastname;
        }

        public void setAge(int age) {
            this.age = age;
        }
        public int getAge() {
            return age;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
        public String getGender() {
            return gender;
        }

        public void setAddress(String address) {
            this.address = address;
        }
        public String getAddress() {
            return address;
        }

        public void setEmployer(String employer) {
            this.employer = employer;
        }
        public String getEmployer() {
            return employer;
        }

        public void setEmail(String email) {
            this.email = email;
        }
        public String getEmail() {
            return email;
        }

        public void setCity(String city) {
            this.city = city;
        }
        public String getCity() {
            return city;
        }

        public void setState(String state) {
            this.state = state;
        }
        public String getState() {
            return state;
        }

        @Override
        public String toString() {
            return "Accout{" +
                    "account_number=" + account_number +
                    ", balance=" + balance +
                    ", firstname='" + firstname + '\'' +
                    ", lastname='" + lastname + '\'' +
                    ", age=" + age +
                    ", gender='" + gender + '\'' +
                    ", address='" + address + '\'' +
                    ", employer='" + employer + '\'' +
                    ", email='" + email + '\'' +
                    ", city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }


}
