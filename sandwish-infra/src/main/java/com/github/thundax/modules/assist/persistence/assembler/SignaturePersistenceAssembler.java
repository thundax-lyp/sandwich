package com.github.thundax.modules.assist.persistence.assembler;

import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import java.util.ArrayList;
import java.util.List;


public final class SignaturePersistenceAssembler {

    private SignaturePersistenceAssembler() {}

    public static SignatureDO toDataObject(Signature entity) {
        if (entity == null) {
            return null;
        }
        SignatureDO dataObject = new SignatureDO();
        dataObject.setBusinessType(entity.getBusinessType());
        dataObject.setBusinessId(entity.getBusinessId());
        dataObject.setSignature(entity.getSignature());
        dataObject.setIsVerifySign(entity.getIsVerifySign());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setUpdateDate(entity.getUpdateDate());
        return dataObject;
    }

    public static Signature toEntity(SignatureDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Signature entity = new Signature();
        entity.setId(dataObject.getBusinessId());
        entity.setBusinessType(dataObject.getBusinessType());
        entity.setBusinessId(dataObject.getBusinessId());
        entity.setSignature(dataObject.getSignature());
        entity.setIsVerifySign(dataObject.getIsVerifySign());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setUpdateDate(dataObject.getUpdateDate());
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
}
