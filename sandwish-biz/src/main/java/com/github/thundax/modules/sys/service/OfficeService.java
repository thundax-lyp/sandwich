package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.query.OfficeQuery;
import java.util.List;

public interface OfficeService extends TreeService<Office> {

    List<Office> list(OfficeQuery query);

    Page<Office> page(OfficeQuery query, Page<Office> page);
}
