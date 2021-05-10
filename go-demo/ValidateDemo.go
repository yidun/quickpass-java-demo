package main

import (
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	simplejson "github.com/bitly/go-simplejson"
	"io/ioutil"
	"net/http"
	"net/url"
	"sort"
	"strconv"
	"strings"
	"time"
)

const (
	apiUrl     = "https://ye.dun.163yun.com/v1/check"  //本机认证服务身份证实人认证在线检测接口地址
	version    = "v1"
	secretId   = "your_secret_id"   //产品密钥ID，产品标识
	secretKey  = "your_secret_key"  //产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
	businessId = "your_business_id" //业务ID，易盾根据产品业务特点分配
)

//请求易盾接口
func check(params url.Values) *simplejson.Json {
	params["secretId"] = []string{secretId}
	params["businessId"] = []string{businessId}
	params["version"] = []string{version}
	params["timestamp"] = []string{strconv.FormatInt(time.Now().UnixNano()/1000000, 10)}
	params["nonce"] = []string{string(make([]byte, 32))}    //32位随机字符串
	params["signature"] = []string{gen_signature(params)}

	resp, err := http.Post(apiUrl, "application/x-www-form-urlencoded", strings.NewReader(params.Encode()))

	if err != nil {
		fmt.Println("调用API接口失败:", err)
		return nil
	}

	defer resp.Body.Close()

	contents, _ := ioutil.ReadAll(resp.Body)
	result, _ := simplejson.NewJson(contents)
	return result
}


//生成签名信息
func gen_signature(params url.Values) string {
	var paramStr string
	keys := make([]string, 0, len(params))
	for k := range params {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	for _, key := range keys {
		paramStr += key + params[key][0]
	}
	paramStr += secretKey
	md5Reader := md5.New()
	md5Reader.Write([]byte(paramStr))
	return hex.EncodeToString(md5Reader.Sum(nil))
}

func main() {
	params := url.Values{
	            //phone为手机号
	            "phone":   []string{"12345678912"},
    	        //token为易盾返回的token
        		"token":   []string{"123456"},
        		//accessToken为运营商预取号获取到的token
        		"accessToken": []string{"123456"},
    }

	ret := check(params)

	code, _ := ret.Get("code").Int()

	if code == 200 {
         result, _ := ret.Get("result").Int()
         if result == 1 {
         	   fmt.Printf("通过,执行后续业务逻辑处理!")
         } else if result == 2 {
               fmt.Printf("不通过,建议进行二次验证,例如短信验证码!")
         } else if result == 3 {
               fmt.Printf("无法确定,建议进行二次验证,例如短信验证码!")
         }
	} else {
		fmt.Printf("建议进行二次验证,例如短信验证码!")
	}
}
