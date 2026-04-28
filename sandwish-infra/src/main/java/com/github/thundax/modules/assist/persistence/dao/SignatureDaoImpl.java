package com.github.thundax.modules.assist.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.dao.SignatureDao;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.persistence.assembler.SignaturePersistenceAssembler;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import com.github.thundax.modules.assist.persistence.mapper.SignatureMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/** 签名 DAO 实现。 */
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
            String businessType,
            String businessId,
            List<String> businessIdList,
            String isVerifySign) {
        return SignaturePersistenceAssembler.toEntityList(
                mapper.selectList(
                        buildListWrapper(businessType, businessId, businessIdList, isVerifySign)));
    }

    @Override
    public Page<Signature> findPage(String businessType, Page<Signature> page) {
        IPage<SignatureDO> dataObjectPage =
                mapper.selectPage(
                        SignaturePageFactory.create(page.getPageNo(), page.getPageSize()),
                        buildListWrapper(businessType, null, null, null));
        page.setPageNo((int) dataObjectPage.getCurrent());
        page.setPageSize((int) dataObjectPage.getSize());
        page.setCount(dataObjectPage.getTotal());
        page.setList(SignaturePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return page;
    }

    @Override
    public int insertOrUpdate(Signature entity) {
        SignatureDO dataObject = SignaturePersistenceAssembler.toDataObject(entity);
        int updated =
                mapper.update(
                        buildUpdateDataObject(dataObject),
                        buildBusinessKeyUpdateWrapper(
                                dataObject.getBusinessType(), dataObject.getBusinessId()));
        if (updated > 0) {
            return updated;
        }
        return mapper.insert(dataObject);
    }

    @Override
    public int delete(String businessType, String businessId) {
        return mapper.delete(buildBusinessKeyWrapper(businessType, businessId));
    }

    private SignatureDO buildUpdateDataObject(SignatureDO dataObject) {
        SignatureDO update = new SignatureDO();
        update.setSignature(dataObject.getSignature());
        update.setIsVerifySign(dataObject.getIsVerifySign());
        update.setUpdateDate(dataObject.getUpdateDate());
        return update;
    }

    private LambdaQueryWrapper<SignatureDO> buildBusinessKeyWrapper(
            String businessType, String businessId) {
        LambdaQueryWrapper<SignatureDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignatureDO::getBusinessType, businessType);
        wrapper.eq(SignatureDO::getBusinessId, businessId);
        return wrapper;
    }

    private LambdaUpdateWrapper<SignatureDO> buildBusinessKeyUpdateWrapper(
            String businessType, String businessId) {
        LambdaUpdateWrapper<SignatureDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SignatureDO::getBusinessType, businessType);
        wrapper.eq(SignatureDO::getBusinessId, businessId);
        return wrapper;
    }

    private LambdaQueryWrapper<SignatureDO> buildListWrapper(
            String businessType,
            String businessId,
            List<String> businessIdList,
            String isVerifySign) {
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
