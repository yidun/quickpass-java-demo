using System;
using System.Collections.Generic;
using System.Net.Http;
using Newtonsoft.Json.Linq;


namespace Com.Netease.Is.QuickPass.Demo
{
    class OneclickDemo
    {
        public static void test()
        {
            /** 产品密钥ID，产品标识 */
            String secretId = "your_secretId";
            /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
            String secretKey = "your_secretKey";
            /** 业务ID，易盾根据产品业务特点分配 */
            String businessId = "your_businessId";
            /** 一键登录在线检测接口地址 */
            String apiUrl = "https://ye.dun.163yun.com/v1/oneclick/check";

            Dictionary<String, String> parameters = new Dictionary<String, String>();
            long curr = (long)(DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds;
            String time = curr.ToString();

            // 1.设置公共参数
            parameters.Add("secretId", secretId);
            parameters.Add("businessId", businessId);
            parameters.Add("version", "v1");
            parameters.Add("timestamp", time);
            parameters.Add("nonce", new Random().Next().ToString());

            // 2.设置私有参数
            parameters.Add("accessToken", "nm6fb6a8a35aa74823a80de8de073995e5"); // 运营商预取号获取到的token
            parameters.Add("token", "088c4952ae8749738937386021eb2531"); // 易盾返回的token
            // accessToken, token都是一次性有效, 且2分钟后自动过期

            // 3.生成签名信息
            String signature = Utils.genSignature(secretKey, parameters);
            parameters.Add("signature", signature);

            // 4.发送HTTP请求
            HttpClient client = Utils.makeHttpClient();
            String result = Utils.doPost(client, apiUrl, parameters, 10000);
            if (result != null)
            {
                JObject ret = JObject.Parse(result);
                int code = ret.GetValue("code").ToObject<Int32>();
                String msg = ret.GetValue("msg").ToObject<String>();
                if (code == 200)
                {
                    JObject data = (JObject)ret.SelectToken("data");
                    String phone = data.GetValue("phone").ToObject<String>();
                    if (phone != null)
                    {
                          // 取号成功, 成功登录系统
                    }
                    else
                    {
                        String resultCode =  data.GetValue("resultCode").ToObject<String>();
                        Console.WriteLine(String.Format("运营商返回码: resultCode={0}", resultCode));
                          // 降级走短信
                    }
                }
                else
                {
                    Console.WriteLine(String.Format("ERROR: code={0}, msg={1}", code, msg));
                    // 降级走短信
                }
            }
            else
            {
                Console.WriteLine("Request failed!");
            }
        }
    }
}