package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;

final class StoragePageFactory {

    private StoragePageFactory() {}

    static IPage<StorageDO> create(int pageNo, int pageSize) {
        return new Page<>(pageNo, pageSize);
    }
}
