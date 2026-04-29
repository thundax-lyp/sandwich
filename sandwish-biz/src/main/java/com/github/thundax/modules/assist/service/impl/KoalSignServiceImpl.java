package com.github.thundax.modules.assist.service.impl;

import static com.github.thundax.common.Constants.QUEUE_PREFIX;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.plugins.koal.sign.SignRequestParam;
import com.github.thundax.modules.assist.plugins.koal.sign.SignResponseParam;
import com.github.thundax.modules.assist.plugins.koal.sign.VerifySignRequestParam;
import com.github.thundax.modules.assist.plugins.koal.sign.VerifySignResponseParam;
import com.github.thundax.modules.assist.service.SignatureService;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 格尔签名服务
 *
 * @author wdit
 */
public class KoalSignServiceImpl extends AbstractSignServiceImpl {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final AmqpTemplate amqpTemplate;

    public static final String QUEUE_SIGN = QUEUE_PREFIX + "sign.koal";

    public static final String DEFAULT_CERT_ALIAS = "SM2";

    public static final String DEFAULT_SERVICE_NAME = "default";

    private static final String URL_SIGN = "/bss/signData";

    private static final String URL_SIGN_NEW = "/bss/signMessageDetach";

    private static final String URL_VERIFY_SIGN = "/bss/verifySignedData";

    private static final String URL_VERIFY_SIGN_NEW = "/bss/verifySignedMessageDetach";

    private final String serviceUrl;

    private final String certAlias;

    private final RestTemplate restTemplate = new RestTemplate();

    public KoalSignServiceImpl(
            AmqpTemplate amqpTemplate, SignatureService signatureService, String serviceUrl, String certAlias) {
        super(signatureService);
        this.amqpTemplate = amqpTemplate;
        this.serviceUrl = serviceUrl;
        this.certAlias = certAlias;
    }

    @Override
    public Boolean sign(String businessType, String businessId, String body) {
        if (StringUtils.isBlank(businessType) || StringUtils.isBlank(businessId) || StringUtils.isBlank(body)) {
            return null;
        }
        Signature signature = new Signature();
        signature.setBusinessType(businessType);
        signature.setBusinessId(businessId);
        signature.setSignature(body);
        signature.setIsVerifySign("0");
        amqpTemplate.convertAndSend(QUEUE_SIGN, JsonUtils.toJson(signature));

        return true;
    }

    @Override
    public Boolean verifySign(String businessType, String businessId, String body) {
        if (StringUtils.isBlank(businessType) || StringUtils.isBlank(businessId) || StringUtils.isBlank(body)) {
            return null;
        }

        Signature signature = signatureService.find(businessType, businessId);
        if (signature == null) {
            return null;
        }
        try {
            VerifySignRequestParam request = new VerifySignRequestParam(
                    new String(Base64.encodeBase64(body.getBytes(StandardCharsets.UTF_8))), signature.getSignature());

            logger.info("验签请求对象：\n{}", JsonUtils.toJson(request));
            String responseString = restTemplate.postForObject(serviceUrl + URL_VERIFY_SIGN_NEW, request, String.class);
            logger.info("验签请求结果：\n{}", responseString);

            VerifySignResponseParam response = JsonUtils.fromJson(responseString, VerifySignResponseParam.class);
            Boolean isSuccess = VerifySignResponseParam.isSuccess(response);
            if (isSuccess) {
                signature.setIsVerifySign("1");
            } else {
                signature.setIsVerifySign("2");
            }
            signatureService.update(signature);
            return isSuccess;

        } catch (RestClientException e) {
            signature.setIsVerifySign("2");
            signatureService.update(signature);
            logger.error("验签异常：{}", e.getMessage());
            return false;
        }
    }

    @RabbitListener(queues = QUEUE_SIGN, concurrency = "1")
    public void sign(String signEntity) {
        if (StringUtils.isBlank(signEntity)) {
            logger.warn("签名内容为空");
            return;
        }
        Signature signaturePre = JsonUtils.fromJson(signEntity, Signature.class);
        try {
            String paramCertAlias = StringUtils.isNotBlank(certAlias) ? certAlias : DEFAULT_CERT_ALIAS;
            SignRequestParam request = new SignRequestParam(
                    Base64.encodeBase64String(signaturePre.getSignature().getBytes(StandardCharsets.UTF_8)),
                    paramCertAlias);

            logger.info("签名请求对象：\n{}", JsonUtils.toJson(request));
            String responseString = restTemplate.postForObject(serviceUrl + URL_SIGN_NEW, request, String.class);
            logger.info("签名请求结果：\n{}", responseString);

            SignResponseParam response = JsonUtils.fromJson(responseString, SignResponseParam.class);
            if (!SignResponseParam.isSuccess(response)) {
                logger.warn("签名对象：{}，签名失败：{}", signEntity, JsonUtils.toJson(response));
                return;
            }

            Signature signature = new Signature();
            signature.setBusinessType(signaturePre.getBusinessType());
            signature.setBusinessId(signaturePre.getBusinessId());
            signature.setIsVerifySign(signaturePre.getIsVerifySign());
            signature.setSignature(response.getB64SignedData());
            Signature existing = signatureService.find(signature.getBusinessType(), signature.getBusinessId());
            if (existing == null) {
                signatureService.add(signature);
            } else {
                signature.setEntityId(EntityIdCodec.toDomain(EntityIdCodec.toValue(existing.getEntityId())));
                signatureService.update(signature);
            }

        } catch (RestClientException e) {
            logger.error("签名对象：{}，签名异常：{}", signEntity, e.getMessage());
        }
    }
}
