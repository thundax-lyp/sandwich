package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.handler.UserDeleteCascadeHandler;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.InOrder;

public class UserServiceImplTest {

    @Test
    public void shouldAddUserAndRewriteUserRoles() {
        UserDao userDao = mock(UserDao.class);
        UserServiceImpl service = new UserServiceImpl(userDao, deleteHandlers());
        User user = new User();

        when(userDao.insert(user)).thenReturn(EntityId.of(1001L));

        EntityId userId = service.add(user, "tester", "encrypted", Arrays.asList(4001L, 4002L));

        assertEquals(Long.valueOf(1001L), EntityIdCodec.toValue(userId));
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).insertUserRole(1001L, Arrays.asList(4001L, 4002L));
    }

    @Test
    public void shouldUpdateUserAndRewriteUserRoles() {
        UserDao userDao = mock(UserDao.class);
        UserServiceImpl service = new UserServiceImpl(userDao, deleteHandlers());
        User user = new User();
        user.setId(EntityId.of(1001L));

        service.update(user, "tester", Collections.singletonList(4001L));

        verify(userDao).update(user);
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).insertUserRole(1001L, Collections.singletonList(4001L));
    }

    @Test
    public void shouldRunDeleteCascadeHandlersBeforeDeletingUser() {
        UserDao userDao = mock(UserDao.class);
        UserDeleteCascadeHandler deleteCascadeHandler = mock(UserDeleteCascadeHandler.class);
        UserServiceImpl service = new UserServiceImpl(userDao, deleteHandlers(deleteCascadeHandler));
        User user = new User();
        user.setId(EntityId.of(1001L));
        when(userDao.getById(EntityId.of(1001L))).thenReturn(user);
        when(userDao.deleteById(EntityId.of(1001L))).thenReturn(1);

        int count = service.deleteById(EntityId.of(1001L));

        assertEquals(1, count);
        InOrder inOrder = org.mockito.Mockito.inOrder(deleteCascadeHandler, userDao);
        inOrder.verify(deleteCascadeHandler).beforeDelete(user);
        inOrder.verify(userDao).deleteUserRole(1001L);
        inOrder.verify(userDao).deleteById(EntityId.of(1001L));
    }

    @Test
    public void shouldAllowNullDeleteCascadeHandlers() {
        UserDao userDao = mock(UserDao.class);
        UserServiceImpl service = new UserServiceImpl(userDao, null);
        User user = new User();
        user.setId(EntityId.of(1001L));
        when(userDao.getById(EntityId.of(1001L))).thenReturn(user);
        when(userDao.deleteById(EntityId.of(1001L))).thenReturn(1);

        int count = service.deleteById(EntityId.of(1001L));

        assertEquals(1, count);
        verify(userDao).deleteUserRole(1001L);
        verify(userDao).deleteById(EntityId.of(1001L));
    }

    private static java.util.List<UserDeleteCascadeHandler> deleteHandlers(UserDeleteCascadeHandler... handlers) {
        return Arrays.asList(handlers);
    }
}
