package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import org.junit.Test;

public class UserIdentityServiceImplTest {

    @Test
    public void shouldGetUserByLoginName() {
        UserDao userDao = mock(UserDao.class);
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userDao, userIdentityDao);
        User user = new User();
        user.setId(EntityId.of("user-1"));
        UserIdentity identity = accountIdentity(user.getId(), "tester");

        when(userIdentityDao.getByIdentity(UserIdentityType.ACCOUNT, "tester")).thenReturn(identity);
        when(userDao.getById(user.getId())).thenReturn(user);

        assertSame(user, service.getByLoginName("tester"));
    }

    @Test
    public void shouldGetAccountLoginName() {
        UserDao userDao = mock(UserDao.class);
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userDao, userIdentityDao);
        EntityId userId = EntityId.of("user-1");

        when(userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT))
                .thenReturn(accountIdentity(userId, "tester"));

        assertEquals("tester", service.getAccountLoginName(userId));
    }

    @Test
    public void shouldUpdateAccountIdentity() {
        UserDao userDao = mock(UserDao.class);
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userDao, userIdentityDao);
        User user = new User();
        user.setId(EntityId.of("user-1"));
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
        identity.setId(EntityId.of("identity-1"));
        identity.setUserId(userId);
        identity.setIdentityType(UserIdentityType.ACCOUNT);
        identity.setIdentityValue(loginName);
        return identity;
    }
}
