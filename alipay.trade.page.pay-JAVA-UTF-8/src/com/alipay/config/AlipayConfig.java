package com.alipay.config;

import java.io.FileWriter;
import java.io.IOException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayConfig {
	
//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
	public static String app_id = "2021000122619334";
	
	// 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCGy5VDWHHAyCzfamjoCmASaXPtkiGbSGjQRHCIYpE54V6J0xQcZgwupXEbbc7ZXD8Sq+3lcDCU1/KttCun4pLYMwrLCW+L7fKmI6H8veSio+tw1bfevi7Jh32COcyWZoXoemREKIJYOmbmOxlyEA6+E6i9TZ9c0UsPXPxl/G/wYdOuLrNnvvi43S+HAqJ1bkHew4N8Bix0N6/ge7MJwqHFhiwthSH3x/phEEA69rPhzy8bB8Xj8XMnSYqVtRnJq8UF6+AW9PQttgATnMjqgwoZ3FW6r8GAMtn4loKMskDmwurVQKDG48LQqrbeKTM4Qz/mNeCrGKVvBghZYvX3bl6XAgMBAAECggEAZzlBzbfOLK5eRpzLR16ioZblsvlkCPETauHuN1McGXHvnqXo0bKIA7SoADQ/4g4VMuw8mqYmXeVdLw45HDCS+UNlMYTBSivm1LmW0+BrVO0oYFkZnFaRp7y9LVurw0UxqI0JeXR4FK5snESHwORxFbgiUQ0/S6nuzA4OLt94rWBuKyISMMpoHQ4D5/5ZfskZ6vEFZZrJQNnJc4WD72K945hPwPDV10AEiNxQO7vl3zvQ2a2JFdEX0ZlD/2SeDiho8zIlrVOIucjnuuyIxCs1HMM+O0noxH/Ihz8onoe0mR3OFpGqd5YrJqAwCSSvE6QbFekRQ1SMAaxxbYFzyDnzEQKBgQDSb5svhGVWuTSd7DYRcn7QcycT1VAKYjEB+mERrErm8v8TY3uDqhxr2TVih7c/OZ+aKHx4HWAZH4F8+2KeuputHH4baDdc0D3uR9B90Zufp1JhQfLDm54Jhho1pLPqoVz3S2b+q/AQ2quA95FrWzObY4GxWL996MTiy1HoUAtaaQKBgQCj+zzb66bBD5kjgFBUu7FbK392D1SYDn2uqZOfM+wdukBcTcF07J2YvP31uvrukX+hUyny5VSd6kB1FkQw04XEGw4fHUqz8vVi6B9BnwxUaqvkNQnACsjOzKlH8dJCX5BdTJGjfID7WqLqiwr/zb36bi/pdRjCNXLPCybstBrQ/wKBgCaZTBjCKyhmHk2dRymaG8K2bIOJy/2rlxuqxmVMegy0o671v3EeIcpydAVXqXgSZMENDg+mK7tP+RtorOU6i1WMdWEnk4gVtlZfm9GIQghd05F9XD8e6zUKAQK37DxsTrYZgZHTDdy5j12/i0/q0aN99AsIoU9CA+MpsiNvkzWJAoGAfIU9s6qAmD4mBcVcOtvEU/z170q47DakzctLpCsjc3eJhA4vh4BeNCvflYgFIMAv8OtcAWyQEtJdXAwqgAE/pKj+0jXGElu+ZvrpMUV7cWcHXGXCH6iQ0Citq0pAZdJ3p8GLHmBe+X/cEsq+8XzkdJTToS4gPOmlYv+O8VPawTUCgYBMCYy7iQD3noGK/6fMnDaQrXbuVmx2MsmXgDaHXI41MYK6EP/J4ql0Lkm/pffsLh65mo1HvMD3iGbX0A+oNdMd5qud4fIqbHYpbZNqk0DmcG/B33sHYDTRuEKckJXuUwlVG9IkKifNnOxoR0x83GfPR1G1Yb0b67u8Xf2b4wv96g==";
	
	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjUxI4lOYwXq5KnS5XK+MX0nMI8EKL8uzHJ0uZdgRzImnLf6dkHHG0h2sSj642cQwphIbtuxDgGb81r0rKMeFp7jLvwlroJMgO43+e9Qg0DAbCHbZSi+/lGwytACMDnoT23Jkw0Ct5CaBlVuKm3DzqvV3aHoFkw55nb9ME1+0/Te5f/yMpNCiCtxOKnQpMZhDqrWwVNqKWWavXW+kfTpFeQ/YArjV/753OqSs6QAgZGUbUXbviRzixNOy62tmft+9IvLtx2KoGvuGDB74+7Qa0xy+lwYQZie+O2RscFU7A/BODCML+VPkFtJeUefvAetLROHcqXjmZF6N0QOpizgCWQIDAQAB";

	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String notify_url = "http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String return_url = "http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";

	// 签名方式
	public static String sign_type = "RSA2";
	
	// 字符编码格式
	public static String charset = "utf-8";
	
	// 支付宝网关
	public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";
	
	// 支付宝网关
	public static String log_path = "D:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /** 
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

