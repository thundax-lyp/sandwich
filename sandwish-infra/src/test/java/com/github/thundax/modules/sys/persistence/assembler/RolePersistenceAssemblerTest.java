package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import org.junit.Test;

public class RolePersistenceAssemblerTest {

    private static final String LEGACY_YES = "1";
    private static final String LEGACY_NO = "0";

    @Test
    public void shouldReadLegacyFlagsAsDomainValues() {
        RoleDO dataObject = new RoleDO();
        dataObject.setAdminFlag(LEGACY_YES);
        dataObject.setEnableFlag(LEGACY_YES);

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

        assertEquals(LEGACY_NO, dataObject.getAdminFlag());
        assertEquals("DISABLED", dataObject.getEnableFlag());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Role entity = new Role();
        entity.setPriority(-1);
        RoleDO dataObject = new RoleDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                RolePersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, RolePersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
