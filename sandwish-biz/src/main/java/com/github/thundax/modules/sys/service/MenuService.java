package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.service.command.ChangeMenuInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeMenuVisibilityCommand;
import com.github.thundax.modules.sys.service.command.CreateMenuCommand;
import com.github.thundax.modules.sys.service.command.MoveMenuCommand;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import java.util.List;

public interface MenuService {

    Menu get(MenuId id);

    List<Menu> list(MenuQuery query);

    PageResult<Menu> page(MenuQuery query, PageQuery page);

    MenuId create(CreateMenuCommand command);

    void changeInfo(ChangeMenuInfoCommand command);

    int remove(MenuId id);

    int changeVisibility(ChangeMenuVisibilityCommand command);

    void move(MoveMenuCommand command);

    boolean existsChildRelation(MenuQuery query);
}
