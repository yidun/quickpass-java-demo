/*
 * @(#) OneclickDemo.java 2020-07-27
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package demo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import demo.utils.HttpClient4Utils;
import demo.utils.SignatureUtils;

/**
 * 本机验证服务端check接口
 * 
 * @author tushenghong01
 * @version 2020-07-27
 */
public class ValidateDemo {

    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 本机认证服务身份证实人认证在线检测接口地址 */
    private final static String API_URL = "https://ye.dun.163yun.com/v1/check";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 100, 2000, 2000, 2000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v1");
        // 格式为时间戳格式, 与当前时间差值不能超过6s
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 32随机字符串
        params.put("nonce", UUID.randomUUID().toString().replace("-", "")); // 32随机字符串
        // 2.设置私有参数d
        params.put("accessToken", "nm6fb6a8a35aa74823a80de8de073995e5"); // 运营商预取号获取到的token
        params.put("token", "088c4952ae8749738937386021eb2531"); // 易盾返回的token
        params.put("phone", "18883110011");

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);
        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        JSONObject resultJson = JSON.parseObject(response);
        if (resultJson.getInteger("code").equals(200)) {
            JSONObject data = resultJson.getJSONObject("data");
            Integer result = data.getInteger("result");
            if (result == 1) {
                // 1通过
                // 执行后续业务逻辑处理
            } else if (result == 2) {
                // 2不通过
                // 建议进行二次验证,例如短信验证码
            } else {
                // 3无法确定
                // 建议进行二次验证,例如短信验证码
            }
        } else {
            // 降级走短信
        }
    }

}
