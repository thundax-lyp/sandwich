package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.UserCredentialDao;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.service.UserIdentityService;
import com.github.thundax.modules.sys.service.UserService;
import org.junit.Test;

public class UserCredentialServiceImplTest {

    @Test
    public void shouldGetPasswordCredentialByAccountIdentity() {
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserCredentialDao userCredentialDao = mock(UserCredentialDao.class);
        UserCredentialServiceImpl service = newService(userIdentityDao, userCredentialDao);
        EntityId userId = EntityId.of("user-1");
        UserIdentity identity = accountIdentity(userId);
        UserCredential credential = new UserCredential();

        when(userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT))
                .thenReturn(identity);
        when(userCredentialDao.getByIdentityIdAndType(identity.getId(), UserCredentialType.PASSWORD))
                .thenReturn(credential);

        assertSame(credential, service.getPasswordCredential(userId));
    }

    @Test
    public void shouldUpdatePasswordCredential() {
        UserService userService = mock(UserService.class);
        UserIdentityService userIdentityService = mock(UserIdentityService.class);
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserCredentialDao userCredentialDao = mock(UserCredentialDao.class);
        SignService signService = mock(SignService.class);
        UserCredentialServiceImpl service = new UserCredentialServiceImpl(
                userService, userIdentityService, userIdentityDao, userCredentialDao, signService);
        EntityId userId = EntityId.of("user-1");
        User user = new User();
        user.setId(userId);
        UserIdentity identity = accountIdentity(userId);
        UserCredential credential = new UserCredential();

        when(userService.getById(userId)).thenReturn(user);
        when(userIdentityService.getAccountLoginName(userId)).thenReturn("tester");
        when(userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT))
                .thenReturn(identity);
        when(userCredentialDao.getByIdentityIdAndType(identity.getId(), UserCredentialType.PASSWORD))
                .thenReturn(credential);

        service.updatePassword(userId, "encrypted-new", "operator-1");

        assertEquals("operator-1", user.getUpdateUserId());
        assertEquals("encrypted-new", credential.getCredentialValue());
        verify(userCredentialDao).update(credential);
        verify(signService).sign(user.getSignName(), user.getSignId(), user.getSignBody());
    }

    private UserCredentialServiceImpl newService(UserIdentityDao userIdentityDao, UserCredentialDao userCredentialDao) {
        return new UserCredentialServiceImpl(
                mock(UserService.class),
                mock(UserIdentityService.class),
                userIdentityDao,
                userCredentialDao,
                mock(SignService.class));
    }

    private UserIdentity accountIdentity(EntityId userId) {
        UserIdentity identity = new UserIdentity();
        identity.setId(EntityId.of("identity-1"));
        identity.setUserId(userId);
        identity.setIdentityType(UserIdentityType.ACCOUNT);
        identity.setIdentityValue("tester");
        return identity;
    }
}
