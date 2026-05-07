package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import org.junit.Test;

public class UserIdentityServiceImplTest {

    @Test
    public void shouldGetIdentityByLoginName() {
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userIdentityDao);
        UserIdentity identity = accountIdentity(EntityId.of(1001L), "tester");

        when(userIdentityDao.getByIdentity(UserIdentityType.ACCOUNT, "tester")).thenReturn(identity);

        assertSame(identity, service.getByLoginName("tester"));
    }

    @Test
    public void shouldGetAccountLoginName() {
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userIdentityDao);
        EntityId userId = EntityId.of(1001L);

        when(userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT))
                .thenReturn(accountIdentity(userId, "tester"));

        assertEquals("tester", service.getAccountLoginName(userId));
    }

    @Test
    public void shouldUpdateAccountIdentity() {
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userIdentityDao);
        User user = new User();
        user.setId(EntityId.of(1001L));
        UserIdentity identity = accountIdentity(user.getId(), "old");

        when(userIdentityDao.getByUserIdAndType(user.getId(), UserIdentityType.ACCOUNT))
                .thenReturn(identity);

        UserIdentity updated = service.updateAccountIdentity(user, "tester");

        assertSame(identity, updated);
        assertEquals("tester", identity.getIdentityValue());
        verify(userIdentityDao).update(identity);
    }

    private UserIdentity accountIdentity(EntityId userId, String loginName) {
        UserIdentity identity = new UserIdentity();
        identity.setId(EntityId.of(2001L));
        identity.setUserId(userId);
        identity.setIdentityType(UserIdentityType.ACCOUNT);
        identity.setIdentityValue(loginName);
        return identity;
    }
}
