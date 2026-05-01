package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.config.Global;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import org.junit.Test;

public class RolePersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyFlagsAsDomainValues() {
        RoleDO dataObject = new RoleDO();
        dataObject.setAdminFlag(Global.YES);
        dataObject.setEnableFlag(Global.ENABLE);

        Role entity = RolePersistenceAssembler.toEntity(dataObject);

        assertSame(RolePrivilege.ADMIN, entity.getPrivilege());
        assertSame(RoleStatus.ENABLED, entity.getStatus());
    }

    @Test
    public void shouldWriteDomainValuesToLegacyFlags() {
        Role entity = new Role();
        entity.setPrivilege(RolePrivilege.NORMAL);
        entity.setStatus(RoleStatus.DISABLED);

        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);

        assertEquals(Global.NO, dataObject.getAdminFlag());
        assertEquals("DISABLED", dataObject.getEnableFlag());
    }
}
