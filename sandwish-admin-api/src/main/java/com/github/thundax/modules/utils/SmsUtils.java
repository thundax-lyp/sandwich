package com.github.thundax.modules.utils;

import com.github.thundax.common.utils.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** @Auther: zhangrudong @Date: 2021/10/29 15:32 @Description: */
public class SmsUtils {
    private static Logger logger = LoggerFactory.getLogger(SmsUtils.class);

    private static SmsProperties properties;

    private static SmsProperties getProperties() {
        if (properties == null) {
            properties = SpringContextHolder.getBean(SmsProperties.class);
        }
        return properties;
    }

    public static Boolean sendMessage(String mobile, String message) {
        logger.info("发送号码mobile：{},发送内容content：{}", mobile, message);
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(message)) {
            return false;
        }
        //        System.out.println(getProperties().getUsername());
        //        System.out.println(getProperties().getPassword());
        //        SmsContent smsContent = create(mobile, message);
        //        logger.info("发送内容：{}",JsonUtils.toJson(smsContent));
        try {
            String result = post(getProperties().getUrl(), generateParams(mobile, message));
            logger.info("发送结果：{}", result);
            if (StringUtils.containsIgnoreCase(result, "success")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
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

    private static String post(String url, Map<String, Object> params) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        if (StringUtils.isBlank(url)) {
            return null;
        }

        RestTemplate client = newRestTemplate(url);
        if (client == null) {
            return null;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<Object> httpEntity = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> response =
                client.exchange(url, HttpMethod.POST, httpEntity, String.class);
        if (HttpStatus.OK.value() == response.getStatusCodeValue()) {
            return response.getBody();
        }
        return "";
    }

    private static RestTemplate newRestTemplate(String url) {
        if (!url.toLowerCase().startsWith("https")) {
            return new RestTemplate();
        }
        try {
            TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
            SSLContext sslContext =
                    SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf =
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            HttpClientBuilder clientBuilder = HttpClients.custom();
            CloseableHttpClient httpClient = clientBuilder.setSSLSocketFactory(sslsf).build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
