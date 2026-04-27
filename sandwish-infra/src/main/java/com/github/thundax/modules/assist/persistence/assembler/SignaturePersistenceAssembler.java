package com.github.thundax.modules.assist.persistence.assembler;

import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import java.util.ArrayList;
import java.util.List;

/** 签名业务模型与持久化对象转换器。 */
public final class SignaturePersistenceAssembler {

    private SignaturePersistenceAssembler() {}

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
        copyQuery(entity.getQuery(), dataObject);
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
        return entity;
    }

    public static List<Signature> toEntityList(List<SignatureDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Signature> entities = new ArrayList<>();
        for (SignatureDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static void copyQuery(Signature.Query query, SignatureDO dataObject) {
        if (query == null) {
            return;
        }
        dataObject.setQueryBusinessType(query.getBusinessType());
        dataObject.setQueryBusinessId(query.getBusinessId());
        dataObject.setQueryIsVerifySign(query.getIsVerifySign());
        dataObject.setQueryBusinessIdList(query.getBusinessIdList());
    }
}
