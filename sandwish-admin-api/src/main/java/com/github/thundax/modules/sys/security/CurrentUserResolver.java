package com.github.thundax.modules.sys.security;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.UserService;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final UserService userService;

    public CurrentUserResolver(UserService userService) {
        this.userService = userService;
    }

    public User currentUser() {
        String subjectId = currentUserId();
        if (StringUtils.isBlank(subjectId)) {
            return new User();
        }
        try {
            User user = userService.get(UserIdCodec.toDomain(Long.valueOf(subjectId)));
            return user == null ? new User() : user;
        } catch (NumberFormatException e) {
            return new User();
        }
    }

    public User requireCurrentUser() {
        User user = currentUser();
        if (user.getId() == null || !user.isEnable()) {
            throw AdminResponseExceptions.invalidToken();
        }
        return user;
    }

    public String currentUserId() {
        SandwishSubject subject = SandwishContextHolder.currentSubject();
        return subject.getSubjectType() == SandwishSubjectType.ADMIN_USER ? subject.getSubjectId() : null;
    }

    public String currentToken() {
        SandwishSubject subject = SandwishContextHolder.currentSubject();
        return subject.getSubjectType() == SandwishSubjectType.ADMIN_USER ? subject.getToken() : null;
    }

    public Set<String> currentAuthorities() {
        return SandwishContextHolder.currentAuthorities();
    }
}
