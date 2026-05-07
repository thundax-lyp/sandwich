package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.persistence.dataobject.UserIdentityDO;
import org.junit.Test;

public class UserIdentityPersistenceAssemblerTest {

    @Test
    public void shouldMapIdentityEntityToDataObject() {
        UserIdentity entity = new UserIdentity();
        entity.setId(EntityId.of(2001L));
        entity.setUserId(EntityId.of(1001L));
        entity.setIdentityType(UserIdentityType.ACCOUNT);
        entity.setIdentityValue("tester");
        entity.setStatus(UserIdentityStatus.ENABLED);

        UserIdentityDO dataObject = UserIdentityPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(2001L), dataObject.getId());
        assertEquals(Long.valueOf(1001L), dataObject.getUserId());
        assertEquals("ACCOUNT", dataObject.getIdentityType());
        assertEquals("tester", dataObject.getIdentityValue());
        assertEquals("ENABLED", dataObject.getStatus());
    }

    @Test
    public void shouldMapIdentityDataObjectToEntity() {
        UserIdentityDO dataObject = new UserIdentityDO();
        dataObject.setId(2001L);
        dataObject.setUserId(1001L);
        dataObject.setIdentityType("account");
        dataObject.setIdentityValue("tester");
        dataObject.setStatus("disabled");

        UserIdentity entity = UserIdentityPersistenceAssembler.toEntity(dataObject);

        assertEquals(Long.valueOf(2001L), entity.getId().value());
        assertEquals(Long.valueOf(1001L), entity.getUserId().value());
        assertSame(UserIdentityType.ACCOUNT, entity.getIdentityType());
        assertEquals("tester", entity.getIdentityValue());
        assertSame(UserIdentityStatus.DISABLED, entity.getStatus());
    }
}
