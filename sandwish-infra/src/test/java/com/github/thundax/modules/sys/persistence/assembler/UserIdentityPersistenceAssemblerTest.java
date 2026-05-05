package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.persistence.dataobject.UserIdentityDO;
import org.junit.Test;

public class UserIdentityPersistenceAssemblerTest {

    @Test
    public void shouldMapIdentityEntityToDataObject() {
        UserIdentity entity = new UserIdentity();
        entity.setId(EntityIdCodec.toDomain("identity-1"));
        entity.setUserId(EntityIdCodec.toDomain("user-1"));
        entity.setIdentityType(UserIdentityType.ACCOUNT);
        entity.setIdentityValue("tester");
        entity.setStatus(UserIdentityStatus.ENABLED);

        UserIdentityDO dataObject = UserIdentityPersistenceAssembler.toDataObject(entity);

        assertEquals("identity-1", dataObject.getId());
        assertEquals("user-1", dataObject.getUserId());
        assertEquals("ACCOUNT", dataObject.getIdentityType());
        assertEquals("tester", dataObject.getIdentityValue());
        assertEquals("ENABLED", dataObject.getStatus());
    }

    @Test
    public void shouldMapIdentityDataObjectToEntity() {
        UserIdentityDO dataObject = new UserIdentityDO();
        dataObject.setId("identity-1");
        dataObject.setUserId("user-1");
        dataObject.setIdentityType("account");
        dataObject.setIdentityValue("tester");
        dataObject.setStatus("disabled");

        UserIdentity entity = UserIdentityPersistenceAssembler.toEntity(dataObject);

        assertEquals("identity-1", entity.getId().value());
        assertEquals("user-1", entity.getUserId().value());
        assertSame(UserIdentityType.ACCOUNT, entity.getIdentityType());
        assertEquals("tester", entity.getIdentityValue());
        assertSame(UserIdentityStatus.DISABLED, entity.getStatus());
    }
}
