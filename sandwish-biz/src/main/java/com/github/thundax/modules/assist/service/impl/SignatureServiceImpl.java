package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.assist.dao.SignatureDao;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.service.SignatureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签名存储SERVICE-IMPL
 *
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class SignatureServiceImpl extends CrudServiceImpl<SignatureDao, Signature> implements SignatureService {

    public SignatureServiceImpl(SignatureDao dao) {
        super(dao);
    }


    @Override
    public Signature find(String businessType, String businessId) {
        if (StringUtils.isBlank(businessType) || StringUtils.isBlank(businessId)) {
            return null;
        }
        Signature query = new Signature();
        query.setBusinessType(businessType);
        query.setBusinessId(businessId);
        return dao.get(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Signature entity) {
        entity.preInsert();
        entity.preUpdate();
        dao.insertOrUpdate(entity);
    }

}
