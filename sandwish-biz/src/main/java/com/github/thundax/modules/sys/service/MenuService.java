package com.github.thundax.modules.sys.service;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import java.util.List;

public interface MenuService extends TreeService<Menu> {

    List<Menu> list(MenuQuery query);

    Page<Menu> page(MenuQuery query, Page<Menu> page);

    List<Menu> list(Integer maxRank);

    List<Menu> listChildren(String parentId);

    int updateVisibility(Menu menu);

    int updateVisibility(List<Menu> list);
}
