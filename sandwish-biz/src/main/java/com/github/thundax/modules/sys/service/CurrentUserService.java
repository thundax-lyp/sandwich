package com.github.thundax.modules.sys.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;

public interface CurrentUserService {

    User updateInfo(User currentUser, String name, String email, String mobile);

    void updatePassword(User currentUser, String oldPassword, String password) throws ApiException;

    List<Menu> listAccessibleMenus(User currentUser);

    List<Menu> listVisibleMenus(User currentUser);
}
