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

        when(userDao.insert(user)).thenReturn(1001L);

        EntityId userId = service.add(user, "tester", "encrypted", Arrays.asList(4001L, 4002L));

        assertEquals(Long.valueOf(1001L), EntityIdCodec.toValue(userId));
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).insertUserRole(1001L, Arrays.asList(4001L, 4002L));
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
        user.setId(EntityId.of(1001L));

        service.update(user, "tester", Collections.singletonList(4001L));

        verify(userDao).update(user);
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).insertUserRole(1001L, Collections.singletonList(4001L));
        verify(userIdentityService).updateAccountIdentity(user, "tester");
        verify(userCredentialService, never()).upsertPassword(user, null);
    }
}
