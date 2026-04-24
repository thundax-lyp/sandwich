package com.github.thundax.modules.assist.persistence.assembler;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;

import java.util.List;

/**
 * 签名业务模型与持久化对象转换器。
 */
public final class SignaturePersistenceAssembler {

    private SignaturePersistenceAssembler() {
    }

    public static SignatureDO toDataObject(Signature entity) {
        if (entity == null) {
            return null;
        }
        SignatureDO dataObject = new SignatureDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setBusinessType(entity.getBusinessType());
        dataObject.setBusinessId(entity.getBusinessId());
        dataObject.setSignature(entity.getSignature());
        dataObject.setIsVerifySign(entity.getIsVerifySign());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setDelFlag(entity.getDelFlag());
        dataObject.setQuery(toDataObjectQuery(entity.getQuery()));
        return dataObject;
    }

    public static Signature toEntity(SignatureDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Signature entity = new Signature();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setBusinessType(dataObject.getBusinessType());
        entity.setBusinessId(dataObject.getBusinessId());
        entity.setSignature(dataObject.getSignature());
        entity.setIsVerifySign(dataObject.getIsVerifySign());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setDelFlag(dataObject.getDelFlag());
        entity.setQuery(toEntityQuery(dataObject.getQuery()));
        return entity;
    }

    public static List<Signature> toEntityList(List<SignatureDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Signature> entities = ListUtils.newArrayList();
        for (SignatureDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static SignatureDO.Query toDataObjectQuery(Signature.Query query) {
        if (query == null) {
            return null;
        }
        SignatureDO.Query dataObjectQuery = new SignatureDO.Query();
        dataObjectQuery.setBusinessType(query.getBusinessType());
        dataObjectQuery.setBusinessId(query.getBusinessId());
        dataObjectQuery.setIsVerifySign(query.getIsVerifySign());
        dataObjectQuery.setBusinessIdList(query.getBusinessIdList());
        return dataObjectQuery;
    }

    private static Signature.Query toEntityQuery(SignatureDO.Query query) {
        if (query == null) {
            return null;
        }
        Signature.Query entityQuery = new Signature.Query();
        entityQuery.setBusinessType(query.getBusinessType());
        entityQuery.setBusinessId(query.getBusinessId());
        entityQuery.setIsVerifySign(query.getIsVerifySign());
        entityQuery.setBusinessIdList(query.getBusinessIdList());
        return entityQuery;
    }
}
