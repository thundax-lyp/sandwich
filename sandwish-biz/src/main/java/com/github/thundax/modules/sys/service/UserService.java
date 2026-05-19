package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.service.command.ChangeUserInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeUserStatusCommand;
import com.github.thundax.modules.sys.service.command.CreateUserCommand;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.List;

public interface UserService {

    User get(UserId id);

    List<User> list(UserQuery query);

    PageResult<User> page(UserQuery query, PageQuery page);

    boolean existsEmail(UserQuery query);

    boolean existsMobile(UserQuery query);

    UserId create(CreateUserCommand command);

    void changeInfo(ChangeUserInfoCommand command);

    int remove(UserId id);

    int changeStatus(ChangeUserStatusCommand command);

    List<Role> listUserRoles(UserQuery query);
}
