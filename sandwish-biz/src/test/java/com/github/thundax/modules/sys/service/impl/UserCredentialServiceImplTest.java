package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserCredentialDao;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.service.UserIdentityService;
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
    public void shouldUpsertPasswordCredential() {
        UserIdentityService userIdentityService = mock(UserIdentityService.class);
        UserIdentityDao userIdentityDao = mock(UserIdentityDao.class);
        UserCredentialDao userCredentialDao = mock(UserCredentialDao.class);
        UserCredentialServiceImpl service =
                new UserCredentialServiceImpl(userIdentityService, userIdentityDao, userCredentialDao);
        EntityId userId = EntityId.of("user-1");
        User user = new User();
        user.setId(userId);
        UserIdentity identity = accountIdentity(userId);
        UserCredential credential = new UserCredential();

        when(userIdentityService.getAccountLoginName(userId)).thenReturn("tester");
        when(userIdentityService.updateAccountIdentity(user, "tester")).thenReturn(identity);
        when(userCredentialDao.getByIdentityIdAndType(identity.getId(), UserCredentialType.PASSWORD))
                .thenReturn(credential);

        service.upsertPassword(user, "encrypted-new");

        assertEquals("encrypted-new", credential.getCredentialValue());
        verify(userCredentialDao).update(credential);
    }

    private UserCredentialServiceImpl newService(UserIdentityDao userIdentityDao, UserCredentialDao userCredentialDao) {
        return new UserCredentialServiceImpl(mock(UserIdentityService.class), userIdentityDao, userCredentialDao);
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
