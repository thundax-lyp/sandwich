package com.github.thundax.modules.assist.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.assist.dao.SignatureDao;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.persistence.assembler.SignaturePersistenceAssembler;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import com.github.thundax.modules.assist.persistence.mapper.SignatureMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SignatureDaoImpl implements SignatureDao {

    private final SignatureMapper mapper;

    public SignatureDaoImpl(SignatureMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Signature find(String businessType, String businessId) {
        return SignaturePersistenceAssembler.toEntity(
                mapper.selectOne(buildBusinessKeyWrapper(businessType, businessId)));
    }

    @Override
    public List<Signature> findByBusinessIds(List<String> businessIdList) {
        return SignaturePersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(null, null, businessIdList, null)));
    }

    @Override
    public List<Signature> findList(
            String businessType, String businessId, List<String> businessIdList, String isVerifySign) {
        return SignaturePersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(businessType, businessId, businessIdList, isVerifySign)));
    }

    @Override
    public Page<Signature> findPage(String businessType, int pageNo, int pageSize) {
        Page<SignatureDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(businessType, null, null, null));
        Page<Signature> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(SignaturePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Signature entity) {
        mapper.insert(SignaturePersistenceAssembler.toDataObject(entity));
        return entity.getBusinessId();
    }

    @Override
    public int update(Signature entity) {
        SignatureDO dataObject = SignaturePersistenceAssembler.toDataObject(entity);
        return mapper.update(
                SignaturePersistenceAssembler.toUpdateDataObject(dataObject),
                buildBusinessKeyWrapper(dataObject.getBusinessType(), dataObject.getBusinessId()));
    }

    @Override
    public int delete(String businessType, String businessId) {
        return mapper.delete(buildBusinessKeyWrapper(businessType, businessId));
    }

    private LambdaQueryWrapper<SignatureDO> buildBusinessKeyWrapper(String businessType, String businessId) {
        LambdaQueryWrapper<SignatureDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignatureDO::getBusinessType, businessType);
        wrapper.eq(SignatureDO::getBusinessId, businessId);
        return wrapper;
    }

    private LambdaQueryWrapper<SignatureDO> buildListWrapper(
            String businessType, String businessId, List<String> businessIdList, String isVerifySign) {
        LambdaQueryWrapper<SignatureDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(businessType)) {
            wrapper.eq(SignatureDO::getBusinessType, businessType);
        }
        if (StringUtils.isNotBlank(businessId)) {
            wrapper.eq(SignatureDO::getBusinessId, businessId);
        }
        if (businessIdList != null) {
            wrapper.in(SignatureDO::getBusinessId, businessIdList);
        }
        if (StringUtils.isNotBlank(isVerifySign)) {
            wrapper.eq(SignatureDO::getIsVerifySign, isVerifySign);
        }
        return wrapper;
    }
}
