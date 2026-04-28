package com.github.thundax.modules.auth.utils;

import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import org.springframework.lang.NonNull;

public class UserAccessHolder {

    private static final PooledThreadLocal<String> USER_ID_HOLDER = new PooledThreadLocal<>();
    private static final PooledThreadLocal<String> TOKEN_HOLDER = new PooledThreadLocal<>();

    /** 获取当前用户 */
    @NonNull
    public static User currentUser() {
        User user = UserServiceHolder.get(currentUserId());
        if (user != null) {
            return user;
        }
        return new User();
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
            USER_ID_HOLDER.remove();
            TOKEN_HOLDER.remove();
        }
    }

    public static String getLoginName() {
        return currentUser().getLoginName();
    }
}
