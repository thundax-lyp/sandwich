package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import com.github.thundax.modules.sys.persistence.dataobject.UserCredentialDO;
import java.util.Date;
import org.junit.Test;

public class UserCredentialPersistenceAssemblerTest {

    @Test
    public void shouldMapCredentialEntityToDataObject() {
        Date lockedUntil = new Date(1000L);
        Date expiresAt = new Date(2000L);
        Date lastVerifiedAt = new Date(3000L);
        UserCredential entity = new UserCredential();
        entity.setId(EntityId.of(3001L));
        entity.setUserId(EntityId.of(1001L));
        entity.setIdentityId(EntityId.of(2001L));
        entity.setCredentialType(UserCredentialType.PASSWORD);
        entity.setCredentialValue("encrypted");
        entity.setStatus(UserCredentialStatus.LOCKED);
        entity.setNeedChangePassword(true);
        entity.setFailedCount(3);
        entity.setFailedLimit(5);
        entity.setLockedUntil(lockedUntil);
        entity.setExpiresAt(expiresAt);
        entity.setLastVerifiedAt(lastVerifiedAt);

        UserCredentialDO dataObject = UserCredentialPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(3001L), dataObject.getId());
        assertEquals(Long.valueOf(1001L), dataObject.getUserId());
        assertEquals(Long.valueOf(2001L), dataObject.getIdentityId());
        assertEquals("PASSWORD", dataObject.getCredentialType());
        assertEquals("encrypted", dataObject.getCredentialValue());
        assertEquals("LOCKED", dataObject.getStatus());
        assertTrue(dataObject.getNeedChangePassword());
        assertEquals(Integer.valueOf(3), dataObject.getFailedCount());
        assertEquals(Integer.valueOf(5), dataObject.getFailedLimit());
        assertEquals(lockedUntil, dataObject.getLockedUntil());
        assertEquals(expiresAt, dataObject.getExpiresAt());
        assertEquals(lastVerifiedAt, dataObject.getLastVerifiedAt());
    }

    @Test
    public void shouldMapCredentialDataObjectToEntityAndNormalizeNullCounters() {
        UserCredentialDO dataObject = new UserCredentialDO();
        dataObject.setId(3001L);
        dataObject.setUserId(1001L);
        dataObject.setIdentityId(2001L);
        dataObject.setCredentialType("password");
        dataObject.setCredentialValue("encrypted");
        dataObject.setStatus("active");

        UserCredential entity = UserCredentialPersistenceAssembler.toEntity(dataObject);

        assertEquals(Long.valueOf(3001L), entity.getId().value());
        assertEquals(Long.valueOf(1001L), entity.getUserId().value());
        assertEquals(Long.valueOf(2001L), entity.getIdentityId().value());
        assertSame(UserCredentialType.PASSWORD, entity.getCredentialType());
        assertEquals("encrypted", entity.getCredentialValue());
        assertSame(UserCredentialStatus.ACTIVE, entity.getStatus());
        assertFalse(entity.isNeedChangePassword());
        assertEquals(0, entity.getFailedCount());
        assertEquals(0, entity.getFailedLimit());
    }
}
