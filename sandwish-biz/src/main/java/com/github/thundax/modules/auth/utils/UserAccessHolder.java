package com.github.thundax.modules.auth.utils;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.UserQuery;
import org.springframework.lang.NonNull;

public class UserAccessHolder {

    private static final PooledThreadLocal<String> USER_ID_HOLDER = new PooledThreadLocal<>();
    private static final PooledThreadLocal<String> TOKEN_HOLDER = new PooledThreadLocal<>();

    @NonNull
    public static User currentUser() {
        User user = SpringContextHolder.getBean(UserService.class)
                .get(userQuery(EntityIdCodec.toDomain(Long.valueOf(currentUserId()))));
        if (user != null) {
            return user;
        }
        return new User();
    }

    private static UserQuery userQuery(EntityId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    public static String currentUserId() {
        return USER_ID_HOLDER.get();
    }

    public static String currentToken() {
        return TOKEN_HOLDER.get();
    }

    public static void currentUserId(String userId, String token) {
        if (userId != null) {
            USER_ID_HOLDER.set(userId);
            TOKEN_HOLDER.set(token);
        } else {
            clear();
        }
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        TOKEN_HOLDER.remove();
    }
}
