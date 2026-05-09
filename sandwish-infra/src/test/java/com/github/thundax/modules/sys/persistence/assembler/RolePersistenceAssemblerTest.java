package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.enums.RolePrivilege;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import org.junit.Test;

public class RolePersistenceAssemblerTest {

    @Test
    public void shouldReadPrivilegeAndEnumStatusAsDomainValues() {
        RoleDO dataObject = new RoleDO();
        dataObject.setPrivilege("ADMIN");
        dataObject.setStatus("ENABLED");

        Role entity = RolePersistenceAssembler.toEntity(dataObject);

        assertSame(RolePrivilege.ADMIN, entity.getPrivilege());
        assertSame(RoleStatus.ENABLED, entity.getStatus());
    }

    @Test
    public void shouldRejectLegacyStatusValue() {
        RoleDO dataObject = new RoleDO();
        dataObject.setStatus("1");

        try {
            RolePersistenceAssembler.toEntity(dataObject);
            fail("Legacy status value must be rejected");
        } catch (BizException expected) {
            assertEquals("Unknown role status: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteDomainValuesToPrivilegeAndEnumStatus() {
        Role entity = new Role();
        entity.setPrivilege(RolePrivilege.NORMAL);
        entity.setStatus(RoleStatus.DISABLED);

        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);

        assertEquals("NORMAL", dataObject.getPrivilege());
        assertEquals("DISABLED", dataObject.getStatus());
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
