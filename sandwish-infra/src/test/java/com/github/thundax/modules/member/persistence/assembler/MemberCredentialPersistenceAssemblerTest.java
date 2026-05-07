package com.github.thundax.modules.member.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import com.github.thundax.modules.member.persistence.dataobject.MemberCredentialDO;
import java.util.Date;
import org.junit.Test;

public class MemberCredentialPersistenceAssemblerTest {

    @Test
    public void shouldMapCredentialToDataObject() {
        Date lockedUntil = new Date(1000L);
        MemberCredential entity = new MemberCredential();
        entity.setId(EntityId.of(1001L));
        entity.setMemberId(EntityId.of(2001L));
        entity.setIdentityId(EntityId.of(3001L));
        entity.setCredentialType(MemberCredentialType.PASSWORD);
        entity.setCredentialValue("hash");
        entity.setStatus(MemberCredentialStatus.ACTIVE);
        entity.setNeedChangePassword(true);
        entity.setFailedCount(2);
        entity.setFailedLimit(5);
        entity.setLockedUntil(lockedUntil);

        MemberCredentialDO dataObject = MemberCredentialPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(1001L), dataObject.getId());
        assertEquals(Long.valueOf(2001L), dataObject.getMemberId());
        assertEquals(Long.valueOf(3001L), dataObject.getIdentityId());
        assertEquals("PASSWORD", dataObject.getCredentialType());
        assertEquals("hash", dataObject.getCredentialValue());
        assertEquals("ACTIVE", dataObject.getStatus());
        assertEquals(Boolean.TRUE, dataObject.getNeedChangePassword());
        assertEquals(Integer.valueOf(2), dataObject.getFailedCount());
        assertEquals(Integer.valueOf(5), dataObject.getFailedLimit());
        assertSame(lockedUntil, dataObject.getLockedUntil());
    }

    @Test
    public void shouldMapCredentialToEntityWithNullDefaults() {
        MemberCredentialDO dataObject = new MemberCredentialDO();
        dataObject.setId(1001L);
        dataObject.setMemberId(2001L);
        dataObject.setIdentityId(3001L);
        dataObject.setCredentialType("PASSWORD");
        dataObject.setCredentialValue("hash");
        dataObject.setStatus("LOCKED");

        MemberCredential entity = MemberCredentialPersistenceAssembler.toEntity(dataObject);

        assertEquals(EntityId.of(1001L), entity.getId());
        assertEquals(EntityId.of(2001L), entity.getMemberId());
        assertEquals(EntityId.of(3001L), entity.getIdentityId());
        assertSame(MemberCredentialType.PASSWORD, entity.getCredentialType());
        assertEquals("hash", entity.getCredentialValue());
        assertSame(MemberCredentialStatus.LOCKED, entity.getStatus());
        assertEquals(false, entity.isNeedChangePassword());
        assertEquals(0, entity.getFailedCount());
        assertEquals(0, entity.getFailedLimit());
    }
}
