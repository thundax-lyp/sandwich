package com.github.thundax.modules.member.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.persistence.dataobject.MemberIdentityDO;
import org.junit.Test;

public class MemberIdentityPersistenceAssemblerTest {

    @Test
    public void shouldMapIdentityToDataObject() {
        MemberIdentity entity = new MemberIdentity();
        entity.setId(EntityId.of(1001L));
        entity.setMemberId(EntityId.of(2001L));
        entity.setIdentityType(MemberIdentityType.MOBILE);
        entity.setIdentityValue("13800138000");
        entity.setStatus(MemberIdentityStatus.ENABLED);

        MemberIdentityDO dataObject = MemberIdentityPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(1001L), dataObject.getId());
        assertEquals(Long.valueOf(2001L), dataObject.getMemberId());
        assertEquals("MOBILE", dataObject.getIdentityType());
        assertEquals("13800138000", dataObject.getIdentityValue());
        assertEquals("ENABLED", dataObject.getStatus());
    }

    @Test
    public void shouldMapIdentityToEntity() {
        MemberIdentityDO dataObject = new MemberIdentityDO();
        dataObject.setId(1001L);
        dataObject.setMemberId(2001L);
        dataObject.setIdentityType("EMAIL");
        dataObject.setIdentityValue("a@example.com");
        dataObject.setStatus("DISABLED");

        MemberIdentity entity = MemberIdentityPersistenceAssembler.toEntity(dataObject);

        assertEquals(EntityId.of(1001L), entity.getId());
        assertEquals(EntityId.of(2001L), entity.getMemberId());
        assertSame(MemberIdentityType.EMAIL, entity.getIdentityType());
        assertEquals("a@example.com", entity.getIdentityValue());
        assertSame(MemberIdentityStatus.DISABLED, entity.getStatus());
    }
}
