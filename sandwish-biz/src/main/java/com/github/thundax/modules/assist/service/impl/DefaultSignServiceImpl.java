package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.codec.digest.DigestUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认签名服务
 *
 * @author wdit
 */
public class DefaultSignServiceImpl extends AbstractSignServiceImpl {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public DefaultSignServiceImpl(SignatureService signatureService) {
        super(signatureService);
    }

    @Override
    public Boolean sign(String businessType, String businessId, String body) {
        if (StringUtils.isBlank(businessType)
                || StringUtils.isBlank(businessId)
                || StringUtils.isBlank(body)) {
            return null;
        }

        Signature signature = new Signature();
        signature.setBusinessType(businessType);
        signature.setBusinessId(businessId);
        signature.setSignature(createSignature(body));
        signature.setIsVerifySign("0");
        signatureService.save(signature);

        return true;
    }

    @Override
    public Boolean verifySign(String businessType, String businessId, String body) {
        if (StringUtils.isBlank(businessType)
                || StringUtils.isBlank(businessId)
                || StringUtils.isBlank(body)) {
            return null;
        }

        Signature signature = signatureService.find(businessType, businessId);
        if (signature == null) {
            return null;
        }
        Boolean isSuccess =
                StringUtils.equalsIgnoreCase(createSignature(body), signature.getSignature());
        if (isSuccess) {
            signature.setIsVerifySign("1");
        } else {
            signature.setIsVerifySign("2");
        }
        signature.preUpdate();
        signatureService.save(signature);
        return isSuccess;
    }

    private String createSignature(String body) {
        return DigestUtils.sm3Hex(body);
    }
}
