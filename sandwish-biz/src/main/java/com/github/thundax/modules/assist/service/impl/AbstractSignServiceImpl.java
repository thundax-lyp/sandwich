package com.github.thundax.modules.assist.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * 抽象签名服务
 *
 * @author wdit
 */
public abstract class AbstractSignServiceImpl implements SignService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final SignatureService signatureService;

    public AbstractSignServiceImpl(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSign(String businessType, String businessId) {
        if (StringUtils.isBlank(businessType) || StringUtils.isBlank(businessId)) {
            return;
        }

        Signature query = new Signature();
        query.setBusinessType(businessType);
        query.setBusinessId(businessId);

        signatureService.delete(query);
    }
}
