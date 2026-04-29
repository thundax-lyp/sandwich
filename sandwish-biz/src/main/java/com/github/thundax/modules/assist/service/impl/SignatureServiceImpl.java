package com.github.thundax.modules.assist.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.dao.SignatureDao;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.service.SignatureService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签名存储SERVICE-IMPL
 *
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class SignatureServiceImpl implements SignatureService {

    private final SignatureDao dao;

    public SignatureServiceImpl(SignatureDao dao) {
        this.dao = dao;
    }

    @Override
    public Signature find(String businessType, String businessId) {
        if (StringUtils.isBlank(businessType) || StringUtils.isBlank(businessId)) {
            return null;
        }
        return dao.find(businessType, businessId);
    }

    @Override
    public Page<Signature> findPage(String businessType, Page<Signature> page) {
        Page<Signature> normalizedPage = normalizePage(page);
        IPage<Signature> dataPage =
                dao.findPage(businessType, normalizedPage.getPageNo(), normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Signature entity) {
        entity.setEntityId(EntityIdCodec.toDomain(dao.insert(entity)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Signature entity) {
        dao.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Signature entity) {
        if (entity == null) {
            return 0;
        }
        return dao.delete(entity.getBusinessType(), entity.getBusinessId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<Signature> list) {
        return batchOperate(list, this::delete);
    }

    private int batchOperate(Collection<Signature> collection, Function<Signature, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Signature entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private Page<Signature> normalizePage(Page<Signature> page) {
        Page<Signature> normalizedPage = page == null ? new Page<>() : page;
        normalizedPage.initialize();
        return normalizedPage;
    }
}
