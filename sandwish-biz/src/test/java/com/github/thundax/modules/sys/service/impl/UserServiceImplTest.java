package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserCredentialService;
import com.github.thundax.modules.sys.service.UserIdentityService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class UserServiceImplTest {

    @Test
    public void shouldAddUserAndDelegateIdentityAndPasswordCredential() {
        UserDao userDao = mock(UserDao.class);
        UserIdentityService userIdentityService = mock(UserIdentityService.class);
        UserCredentialService userCredentialService = mock(UserCredentialService.class);
        UserServiceImpl service = new UserServiceImpl(userDao, userIdentityService, userCredentialService);
        User user = new User();

        when(userDao.insert(user)).thenReturn("user-1");

        EntityId userId = service.add(user, "tester", "encrypted", Arrays.asList("role-1", "role-2"));

        assertEquals("user-1", EntityIdCodec.toValue(userId));
        verify(userDao).deleteUserRole("user-1");
        verify(userDao).insertUserRole("user-1", Arrays.asList("role-1", "role-2"));
        verify(userIdentityService).updateAccountIdentity(user, "tester");
        verify(userCredentialService).upsertPassword(user, "encrypted");
    }

    @Test
    public void shouldUpdateUserAndDelegateAccountIdentityWithoutPasswordCredential() {
        UserDao userDao = mock(UserDao.class);
        UserIdentityService userIdentityService = mock(UserIdentityService.class);
        UserCredentialService userCredentialService = mock(UserCredentialService.class);
        UserServiceImpl service = new UserServiceImpl(userDao, userIdentityService, userCredentialService);
        User user = new User();
        user.setId(EntityId.of("user-1"));

        service.update(user, "tester", Collections.singletonList("role-1"));

        verify(userDao).update(user);
        verify(userDao).deleteUserRole("user-1");
        verify(userDao).insertUserRole("user-1", Collections.singletonList("role-1"));
        verify(userIdentityService).updateAccountIdentity(user, "tester");
        verify(userCredentialService, never()).upsertPassword(user, null);
    }
}
