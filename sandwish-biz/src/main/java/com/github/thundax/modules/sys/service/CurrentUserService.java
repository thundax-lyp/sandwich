package com.github.thundax.modules.sys.service;

import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserAvatarCommand;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserPasswordCommand;
import com.github.thundax.modules.sys.service.command.RemoveCurrentUserAvatarCommand;
import com.github.thundax.modules.sys.service.query.CurrentUserQuery;
import java.io.InputStream;
import java.util.List;

public interface CurrentUserService {

    User changeInfo(ChangeCurrentUserInfoCommand command);

    void changePassword(ChangeCurrentUserPasswordCommand command);

    StoredObject changeAvatar(ChangeCurrentUserAvatarCommand command);

    void removeAvatar(RemoveCurrentUserAvatarCommand command);

    StoredObject getAvatar(UserId userId);

    InputStream getAvatarInputStream(UserId userId);

    boolean existsAvatar(UserId userId);

    List<Menu> listAccessibleMenus(CurrentUserQuery query);

    List<Menu> listVisibleMenus(CurrentUserQuery query);
}
