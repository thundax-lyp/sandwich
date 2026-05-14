package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import org.junit.Test;

public class UserPersistenceAssemblerTest {

    @Test
    public void shouldReadPrivilegeAndEnumStatusAsDomainValues() {
        UserDO dataObject = new UserDO();
        dataObject.setPrivilege("SUPER");
        dataObject.setStatus("ENABLED");

        User entity = UserPersistenceAssembler.toEntity(dataObject);

        assertSame(UserPrivilege.SUPER, entity.getPrivilege());
        assertSame(UserStatus.ENABLED, entity.getStatus());
    }

    @Test
    public void shouldRejectLegacyStatusValue() {
        UserDO dataObject = new UserDO();
        dataObject.setStatus("1");

        try {
            UserPersistenceAssembler.toEntity(dataObject);
            fail("Legacy status value must be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Unknown user status: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteDomainValuesToPrivilegeAndEnumStatus() {
        User entity = new User();
        entity.setPrivilege(UserPrivilege.ADMIN);
        entity.setStatus(UserStatus.DISABLED);

        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);

        assertEquals("ADMIN", dataObject.getPrivilege());
        assertEquals("DISABLED", dataObject.getStatus());
    }

    @Test
    public void shouldMapRankValueObjectToRanksColumn() {
        User entity = new User();
        entity.setRank(AccessRank.of(12));

        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        assertEquals(Integer.valueOf(9), dataObject.getRanks());

        dataObject.setRanks(-1);
        User restored = UserPersistenceAssembler.toEntity(dataObject);
        assertEquals(AccessRank.of(0), restored.getRank());
    }
}
