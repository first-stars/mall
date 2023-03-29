package com.w.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.w.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public String app_id = "2021000122619334";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCGy5VDWHHAyCzfamjoCmASaXPtkiGbSGjQRHCIYpE54V6J0xQcZgwupXEbbc7ZXD8Sq+3lcDCU1/KttCun4pLYMwrLCW+L7fKmI6H8veSio+tw1bfevi7Jh32COcyWZoXoemREKIJYOmbmOxlyEA6+E6i9TZ9c0UsPXPxl/G/wYdOuLrNnvvi43S+HAqJ1bkHew4N8Bix0N6/ge7MJwqHFhiwthSH3x/phEEA69rPhzy8bB8Xj8XMnSYqVtRnJq8UF6+AW9PQttgATnMjqgwoZ3FW6r8GAMtn4loKMskDmwurVQKDG48LQqrbeKTM4Qz/mNeCrGKVvBghZYvX3bl6XAgMBAAECggEAZzlBzbfOLK5eRpzLR16ioZblsvlkCPETauHuN1McGXHvnqXo0bKIA7SoADQ/4g4VMuw8mqYmXeVdLw45HDCS+UNlMYTBSivm1LmW0+BrVO0oYFkZnFaRp7y9LVurw0UxqI0JeXR4FK5snESHwORxFbgiUQ0/S6nuzA4OLt94rWBuKyISMMpoHQ4D5/5ZfskZ6vEFZZrJQNnJc4WD72K945hPwPDV10AEiNxQO7vl3zvQ2a2JFdEX0ZlD/2SeDiho8zIlrVOIucjnuuyIxCs1HMM+O0noxH/Ihz8onoe0mR3OFpGqd5YrJqAwCSSvE6QbFekRQ1SMAaxxbYFzyDnzEQKBgQDSb5svhGVWuTSd7DYRcn7QcycT1VAKYjEB+mERrErm8v8TY3uDqhxr2TVih7c/OZ+aKHx4HWAZH4F8+2KeuputHH4baDdc0D3uR9B90Zufp1JhQfLDm54Jhho1pLPqoVz3S2b+q/AQ2quA95FrWzObY4GxWL996MTiy1HoUAtaaQKBgQCj+zzb66bBD5kjgFBUu7FbK392D1SYDn2uqZOfM+wdukBcTcF07J2YvP31uvrukX+hUyny5VSd6kB1FkQw04XEGw4fHUqz8vVi6B9BnwxUaqvkNQnACsjOzKlH8dJCX5BdTJGjfID7WqLqiwr/zb36bi/pdRjCNXLPCybstBrQ/wKBgCaZTBjCKyhmHk2dRymaG8K2bIOJy/2rlxuqxmVMegy0o671v3EeIcpydAVXqXgSZMENDg+mK7tP+RtorOU6i1WMdWEnk4gVtlZfm9GIQghd05F9XD8e6zUKAQK37DxsTrYZgZHTDdy5j12/i0/q0aN99AsIoU9CA+MpsiNvkzWJAoGAfIU9s6qAmD4mBcVcOtvEU/z170q47DakzctLpCsjc3eJhA4vh4BeNCvflYgFIMAv8OtcAWyQEtJdXAwqgAE/pKj+0jXGElu+ZvrpMUV7cWcHXGXCH6iQ0Citq0pAZdJ3p8GLHmBe+X/cEsq+8XzkdJTToS4gPOmlYv+O8VPawTUCgYBMCYy7iQD3noGK/6fMnDaQrXbuVmx2MsmXgDaHXI41MYK6EP/J4ql0Lkm/pffsLh65mo1HvMD3iGbX0A+oNdMd5qud4fIqbHYpbZNqk0DmcG/B33sHYDTRuEKckJXuUwlVG9IkKifNnOxoR0x83GfPR1G1Yb0b67u8Xf2b4wv96g==";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjUxI4lOYwXq5KnS5XK+MX0nMI8EKL8uzHJ0uZdgRzImnLf6dkHHG0h2sSj642cQwphIbtuxDgGb81r0rKMeFp7jLvwlroJMgO43+e9Qg0DAbCHbZSi+/lGwytACMDnoT23Jkw0Ct5CaBlVuKm3DzqvV3aHoFkw55nb9ME1+0/Te5f/yMpNCiCtxOKnQpMZhDqrWwVNqKWWavXW+kfTpFeQ/YArjV/753OqSs6QAgZGUbUXbviRzixNOy62tmft+9IvLtx2KoGvuGDB74+7Qa0xy+lwYQZie+O2RscFU7A/BODCML+VPkFtJeUefvAetLROHcqXjmZF6N0QOpizgCWQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public String notify_url = "http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public String return_url = "http://member.gulimall.com/memberOrder.html";


    // 签名方式
    public String sign_type = "RSA2";

    // 字符编码格式
    public String charset = "utf-8";

    // 支付宝网关
    public String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    //订单超时时间
    private String timeout = "1m";



    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
