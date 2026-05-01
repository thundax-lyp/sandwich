package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.config.Global;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import org.junit.Test;

public class UserPersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyFlagsAsDomainValues() {
        UserDO dataObject = new UserDO();
        dataObject.setSuperFlag(Global.YES);
        dataObject.setAdminFlag(Global.NO);
        dataObject.setEnableFlag(Global.ENABLE);

        User entity = UserPersistenceAssembler.toEntity(dataObject);

        assertSame(UserPrivilege.SUPER, entity.getPrivilege());
        assertSame(UserStatus.ENABLED, entity.getStatus());
    }

    @Test
    public void shouldWriteDomainValuesToLegacyFlags() {
        User entity = new User();
        entity.setPrivilege(UserPrivilege.ADMIN);
        entity.setStatus(UserStatus.DISABLED);

        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);

        assertEquals(Global.NO, dataObject.getSuperFlag());
        assertEquals(Global.YES, dataObject.getAdminFlag());
        assertEquals("DISABLED", dataObject.getEnableFlag());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        User entity = new User();
        entity.setPriority(-1);
        UserDO dataObject = new UserDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                UserPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, UserPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
