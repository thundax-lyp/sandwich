package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;

final class LogPageFactory {

    private LogPageFactory() {}

    static IPage<LogDO> create(int pageNo, int pageSize) {
        return new Page<>(pageNo, pageSize);
    }
}
