var utils = require("./utils");
//产品密钥ID，产品标识 
var secretId = "your_secret_id";
// 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 
var secretKey = "your_secret_key";
// 业务ID，易盾根据产品业务特点分配 
var businessId = "your_business_id";
// 本机验证服务端check接口
var apiurl = "https://ye.dun.163yun.com/v1/check";
//请求参数
var post_data = {
	// 1.设置公有有参数
	secretId: secretId,
	businessId: businessId,
	version: "v1",
	timestamp: new Date().getTime(),
	nonce: utils.noncer()
};
// 2.设置私有属性
post_data.accessToken = "nm6fb6a8a35aa74823a80de8de073995e5";// 运营商预取号获取到的token
post_data.token = "088c4952ae8749738937386021eb2531";// 易盾返回的token
post_data.phone = "18883110011";
var signature = utils.genSignature(secretKey, post_data);
post_data.signature = signature;
//http请求结果
var responseCallback = function (responseData) {
	var data = JSON.parse(responseData);
	var code = data.code;
	var msg = data.msg;
	// 检测结果
	if (code == 200) {
		console.log("data:" + data);
	} else {
		console.log('ERROR:code=' + code + ',msg=' + msg);
	}
}
utils.sendHttpRequest(apiurl, "POST", post_data, responseCallback);
