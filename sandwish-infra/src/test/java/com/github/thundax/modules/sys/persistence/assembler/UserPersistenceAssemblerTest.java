package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import org.junit.Test;

public class UserPersistenceAssemblerTest {

    private static final String LEGACY_YES = "1";
    private static final String LEGACY_NO = "0";

    @Test
    public void shouldReadLegacyFlagsAsDomainValues() {
        UserDO dataObject = new UserDO();
        dataObject.setSuperFlag(LEGACY_YES);
        dataObject.setAdminFlag(LEGACY_NO);
        dataObject.setEnableFlag(LEGACY_YES);

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

        assertEquals(LEGACY_NO, dataObject.getSuperFlag());
        assertEquals(LEGACY_YES, dataObject.getAdminFlag());
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
