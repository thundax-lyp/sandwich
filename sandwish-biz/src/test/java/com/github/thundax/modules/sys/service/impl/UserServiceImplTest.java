package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.User;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class UserServiceImplTest {

    @Test
    public void shouldAddUserAndDelegateIdentityAndPasswordCredential() {
        UserDao userDao = mock(UserDao.class);
        PrincipalIdentityService principalIdentityService = mock(PrincipalIdentityService.class);
        PrincipalCredentialService principalCredentialService = mock(PrincipalCredentialService.class);
        UserServiceImpl service = new UserServiceImpl(userDao, principalIdentityService, principalCredentialService);
        User user = new User();

        when(userDao.insert(user)).thenReturn(EntityId.of(1001L));
        doAnswer(invocation -> {
                    PrincipalIdentity identity = invocation.getArgument(0);
                    identity.setId(EntityId.of(2001L));
                    return EntityId.of(2001L);
                })
                .when(principalIdentityService)
                .add(org.mockito.ArgumentMatchers.any(PrincipalIdentity.class));

        EntityId userId = service.add(user, "tester", "encrypted", Arrays.asList(4001L, 4002L));

        assertEquals(Long.valueOf(1001L), EntityIdCodec.toValue(userId));
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).insertUserRole(1001L, Arrays.asList(4001L, 4002L));
        verify(principalIdentityService)
                .add(org.mockito.ArgumentMatchers.argThat(
                        identity -> PrincipalIdentityType.USER_ACCOUNT == identity.getType()
                                && PrincipalIdentityStatus.ENABLED == identity.getStatus()
                                && "tester".equals(identity.getIdentityValue())));
        verify(principalCredentialService)
                .add(org.mockito.ArgumentMatchers.argThat(
                        credential -> PrincipalCredentialType.USER_PASSWORD == credential.getCredentialType()
                                && EntityId.of(2001L).equals(credential.getIdentityId())
                                && "encrypted".equals(credential.getCredentialValue())));
    }

    @Test
    public void shouldUpdateUserAndDelegateAccountIdentityWithoutPasswordCredential() {
        UserDao userDao = mock(UserDao.class);
        PrincipalIdentityService principalIdentityService = mock(PrincipalIdentityService.class);
        PrincipalCredentialService principalCredentialService = mock(PrincipalCredentialService.class);
        UserServiceImpl service = new UserServiceImpl(userDao, principalIdentityService, principalCredentialService);
        User user = new User();
        user.setId(EntityId.of(1001L));
        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setId(EntityId.of(2001L));
        when(principalIdentityService.getByPrincipalKeyAndType(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(PrincipalIdentityType.USER_ACCOUNT)))
                .thenReturn(identity);

        service.update(user, "tester", Collections.singletonList(4001L));

        verify(userDao).update(user);
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).insertUserRole(1001L, Collections.singletonList(4001L));
        verify(principalIdentityService).update(identity);
        verify(principalCredentialService, never()).update(org.mockito.ArgumentMatchers.any(PrincipalCredential.class));
    }
}
