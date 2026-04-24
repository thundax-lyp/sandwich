package com.github.thundax.modules.utils;

import com.github.thundax.common.utils.HttpUtils;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zhangrudong
 * @Date: 2021/10/29 15:32
 * @Description:
 */
public class SmsUtils {
    private static Logger logger = LoggerFactory.getLogger(SmsUtils.class);

    private static SmsProperties properties;


    private static SmsProperties getProperties() {
        if (properties == null) {
            properties = SpringContextHolder.getBean(SmsProperties.class);
        }
        return properties;
    }

    public static Boolean sendMessage(String mobile,String message){
        logger.info("发送号码mobile：{},发送内容content：{}",mobile,message);
        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(message)){
            return false;
        }
//        System.out.println(getProperties().getUsername());
//        System.out.println(getProperties().getPassword());
//        SmsContent smsContent = create(mobile, message);
//        logger.info("发送内容：{}",JsonUtils.toJson(smsContent));
        try {
            String result = HttpUtils.post(getProperties().getUrl(), generateParams(mobile, message));
            logger.info("发送结果：{}",result);
            if(StringUtils.containsIgnoreCase(result,"success")){
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 创建请求对象
     *
     * @param mobile
     * @param content
     * @return
     */
    private static SmsContent create(String mobile,String content){
        SmsContent smsContent = new SmsContent();
        smsContent.setUserName(getProperties().getUsername());
        smsContent.setPassword(getProperties().getPassword());
        smsContent.setMobilePhone(mobile);
        smsContent.setContent(content);
        smsContent.setFsyhm(getProperties().getFsyhm());
        smsContent.setFsyhxm(getProperties().getFsyhxm());
        smsContent.setFsyhdw(getProperties().getFsyhdw());
        return smsContent;
    }

    /**
     * 生成请求参数
     *
     * @param mobile
     * @param content
     * @return
     */
    private static Map<String, Object> generateParams(String mobile, String content) {
        Map<String, Object> params = new HashMap<>();
        params.put("userName", getProperties().getUsername());
        params.put("password", getProperties().getPassword());
        params.put("mobilePhone", mobile);
        params.put("content", content);
        params.put("fsyhm", getProperties().getFsyhm());
        params.put("fsyhxm", getProperties().getFsyhxm());
        params.put("fsyhdw", getProperties().getFsyhdw());
        return params;
    }
}
