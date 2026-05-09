package com.github.thundax.modules.member.persistence.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberGender;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import java.util.ArrayList;
import java.util.List;

public final class MemberPersistenceAssembler {

    private MemberPersistenceAssembler() {}

    public static MemberDO toDataObject(Member entity) {
        if (entity == null) {
            return null;
        }
        MemberDO dataObject = new MemberDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setGender(genderValue(entity.getGender()));
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static Member toEntity(MemberDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Member entity = new Member();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setName(dataObject.getName());
        entity.setGender(genderFrom(dataObject.getGender()));
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
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

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String statusValue(MemberStatus status) {
        return status == null ? null : status.value();
    }

    private static MemberStatus statusFrom(String status) {
        return status == null ? null : MemberStatus.from(status);
    }

    private static String genderValue(MemberGender gender) {
        return gender == null ? null : gender.value();
    }

    private static MemberGender genderFrom(String gender) {
        return gender == null ? null : MemberGender.from(gender);
    }
}
