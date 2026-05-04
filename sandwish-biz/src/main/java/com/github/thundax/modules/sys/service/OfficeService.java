package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.query.OfficeQuery;
import java.util.List;

public interface OfficeService extends TreeService<Office> {

    List<Office> list(OfficeQuery query);

    PageDTO<Office> page(OfficeQuery query, PageDTO<Office> page);
}
