package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.UserIdentity;
import com.github.thundax.modules.auth.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.UserIdentityType;
import com.github.thundax.modules.auth.persistence.dataobject.UserIdentityDO;
import java.util.Date;
import org.junit.Test;

public class UserIdentityPersistenceAssemblerTest {

    @Test
    public void shouldMapIdentityEntityToDataObject() {
        Date createDate = new Date(1000L);
        Date updateDate = new Date(2000L);
        UserIdentity entity = new UserIdentity();
        entity.setId(EntityIdCodec.toDomain("identity-1"));
        entity.setUserId(EntityIdCodec.toDomain("user-1"));
        entity.setIdentityType(UserIdentityType.ACCOUNT);
        entity.setIdentityValue("tester");
        entity.setStatus(UserIdentityStatus.ENABLED);
        entity.setCreateDate(createDate);
        entity.setCreateUserId("admin");
        entity.setUpdateDate(updateDate);
        entity.setUpdateUserId("operator");

        UserIdentityDO dataObject = UserIdentityPersistenceAssembler.toDataObject(entity);

        assertEquals("identity-1", dataObject.getId());
        assertEquals("user-1", dataObject.getUserId());
        assertEquals("ACCOUNT", dataObject.getIdentityType());
        assertEquals("tester", dataObject.getIdentityValue());
        assertEquals("ENABLED", dataObject.getStatus());
        assertEquals(createDate, dataObject.getCreateDate());
        assertEquals("admin", dataObject.getCreateBy());
        assertEquals(updateDate, dataObject.getUpdateDate());
        assertEquals("operator", dataObject.getUpdateBy());
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
