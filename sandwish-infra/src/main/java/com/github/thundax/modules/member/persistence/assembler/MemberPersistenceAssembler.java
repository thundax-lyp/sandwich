package com.github.thundax.modules.member.persistence.assembler;

import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import java.util.ArrayList;
import java.util.List;

/** 会员业务模型与持久化对象转换器。 */
public final class MemberPersistenceAssembler {

    private MemberPersistenceAssembler() {}

    public static MemberDO toDataObject(Member entity) {
        if (entity == null) {
            return null;
        }
        MemberDO dataObject = new MemberDO();
        dataObject.setId(entity.getId());
        dataObject.setIsNewRecord(entity.getIsNewRecord());
        dataObject.setLoginName(entity.getLoginName());
        dataObject.setLoginPass(entity.getLoginPass());
        dataObject.setEmail(entity.getEmail());
        dataObject.setName(entity.getName());
        dataObject.setGender(entity.getGender());
        dataObject.setMobile(entity.getMobile());
        dataObject.setAddress(entity.getAddress());
        dataObject.setZipcode(entity.getZipcode());
        dataObject.setEnableFlag(entity.getEnableFlag());
        dataObject.setRegisterIp(entity.getRegisterIp());
        dataObject.setRegisterDate(entity.getRegisterDate());
        dataObject.setLastLoginIp(entity.getLastLoginIp());
        dataObject.setLastLoginDate(entity.getLastLoginDate());
        dataObject.setYwtbId(entity.getYwtbId());
        dataObject.setLoginCount(entity.getLoginCount());
        dataObject.setPriority(entity.getPriority());
        dataObject.setRemarks(entity.getRemarks());
        dataObject.setCreateDate(entity.getCreateDate());
        dataObject.setCreateUserId(entity.getCreateUserId());
        dataObject.setUpdateDate(entity.getUpdateDate());
        dataObject.setUpdateUserId(entity.getUpdateUserId());
        dataObject.setDelFlag(entity.getDelFlag());
        return dataObject;
    }

    public static Member toEntity(MemberDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Member entity = new Member();
        entity.setId(dataObject.getId());
        entity.setIsNewRecord(dataObject.getIsNewRecord());
        entity.setLoginName(dataObject.getLoginName());
        entity.setLoginPass(dataObject.getLoginPass());
        entity.setEmail(dataObject.getEmail());
        entity.setName(dataObject.getName());
        entity.setGender(dataObject.getGender());
        entity.setMobile(dataObject.getMobile());
        entity.setAddress(dataObject.getAddress());
        entity.setZipcode(dataObject.getZipcode());
        entity.setEnableFlag(dataObject.getEnableFlag());
        entity.setRegisterIp(dataObject.getRegisterIp());
        entity.setRegisterDate(dataObject.getRegisterDate());
        entity.setLastLoginIp(dataObject.getLastLoginIp());
        entity.setLastLoginDate(dataObject.getLastLoginDate());
        entity.setYwtbId(dataObject.getYwtbId());
        entity.setLoginCount(dataObject.getLoginCount());
        entity.setPriority(dataObject.getPriority());
        entity.setRemarks(dataObject.getRemarks());
        entity.setCreateDate(dataObject.getCreateDate());
        entity.setCreateUserId(dataObject.getCreateUserId());
        entity.setUpdateDate(dataObject.getUpdateDate());
        entity.setUpdateUserId(dataObject.getUpdateUserId());
        entity.setDelFlag(dataObject.getDelFlag());
        return entity;
    }

    public static List<Member> toEntityList(List<MemberDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Member> entities = new ArrayList<>();
        for (MemberDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }
}
