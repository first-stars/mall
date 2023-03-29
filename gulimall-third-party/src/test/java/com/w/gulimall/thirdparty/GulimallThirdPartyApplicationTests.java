package com.w.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;


    @Test
    public void testOss() throws FileNotFoundException {
        InputStream fileInputStream = new FileInputStream("E:\\img\\test.png");
        ossClient.putObject("gulimalldev", "test02.png",fileInputStream);
        System.out.println("上传成功");
    }
}
