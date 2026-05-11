package com.github.thundax.modules.sys.service;

import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserPasswordCommand;
import com.github.thundax.modules.sys.service.query.CurrentUserQuery;
import java.util.List;

public interface CurrentUserService {

    User changeInfo(ChangeCurrentUserInfoCommand command);

    void changePassword(ChangeCurrentUserPasswordCommand command);

    List<Menu> listAccessibleMenus(CurrentUserQuery query);

    List<Menu> listVisibleMenus(CurrentUserQuery query);
}
