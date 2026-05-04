package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityIdCodec;
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
        entity.setId(EntityIdCodec.toDomain("credential-1"));
        entity.setUserId(EntityIdCodec.toDomain("user-1"));
        entity.setIdentityId(EntityIdCodec.toDomain("identity-1"));
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

        assertEquals("credential-1", dataObject.getId());
        assertEquals("user-1", dataObject.getUserId());
        assertEquals("identity-1", dataObject.getIdentityId());
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
        dataObject.setId("credential-1");
        dataObject.setUserId("user-1");
        dataObject.setIdentityId("identity-1");
        dataObject.setCredentialType("password");
        dataObject.setCredentialValue("encrypted");
        dataObject.setStatus("active");

        UserCredential entity = UserCredentialPersistenceAssembler.toEntity(dataObject);

        assertEquals("credential-1", entity.getId().value());
        assertEquals("user-1", entity.getUserId().value());
        assertEquals("identity-1", entity.getIdentityId().value());
        assertSame(UserCredentialType.PASSWORD, entity.getCredentialType());
        assertEquals("encrypted", entity.getCredentialValue());
        assertSame(UserCredentialStatus.ACTIVE, entity.getStatus());
        assertFalse(entity.isNeedChangePassword());
        assertEquals(0, entity.getFailedCount());
        assertEquals(0, entity.getFailedLimit());
    }
}
