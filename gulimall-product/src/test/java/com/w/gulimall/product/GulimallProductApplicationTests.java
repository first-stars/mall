package com.w.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.w.gulimall.product.dao.SkuSaleAttrValueDao;
import com.w.gulimall.product.entity.BrandEntity;
import com.w.gulimall.product.service.AttrGroupService;
import com.w.gulimall.product.service.BrandService;
import com.w.gulimall.product.service.CategoryService;
import com.w.gulimall.product.service.SkuInfoService;
import com.w.gulimall.product.vo.SkuItemSaleAttrVo;
import com.w.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * 1、引入oss-starter
 * 2、配置key，endpoint相关信息即可
 * 3、使用OSSClient 进行相关操作
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void  setSkuSaleAttrValueTest(){
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(13l);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    public void skuItemTest(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(13l, 225l);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }

    @Test
    public void testRedis(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","word+"+ UUID.randomUUID().toString());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void testFindePath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径:{}", Arrays.asList(catelogPath));
    }



    @Test
    public void test1() throws FileNotFoundException {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5t8A7WvsizBCpPk61gpA";
//        String accessKeySecret = "FRfgLyKa5mgRcSqyXfjAx2ySyBQLUk";
//        // 填写Bucket名称，例如examplebucket。
//        String bucketName = "gulimalldev";
//        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
//        String objectName = "test.png";
//        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
//        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//        String filePath= "E:\\img\\test.png";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        try {
//            InputStream inputStream = new FileInputStream(filePath);
//            // 创建PutObjectRequest对象。
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
//            // 设置该属性可以返回response。如果不设置，则返回的response为空。
//            putObjectRequest.setProcess("true");
//            // 创建PutObject请求。
//            PutObjectResult result = ossClient.putObject(putObjectRequest);
//            // 如果上传成功，则返回200。
//            System.out.println(result.getResponse().getStatusCode());
//        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
//        } catch (ClientException ce) {
//            System.out.println("Caught an ClientException, which means the client encountered "
//                    + "a serious internal problem while trying to communicate with OSS, "
//                    + "such as not being able to access the network.");
//            System.out.println("Error Message:" + ce.getMessage());
//        } finally {
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
//        System.out.println("上传完成");
    }





    @Test
    public void contextLoads() {

//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");

//
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功....");

//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1l));
        list.forEach((item) -> {
            System.out.println(item);
        });


//        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
//        list.forEach((item) -> {
//            System.out.println(item);
//        });

    }

}

