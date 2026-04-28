package com.github.thundax.modules.assist.persistence.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;

final class SignaturePageFactory {

    private SignaturePageFactory() {}

    static IPage<SignatureDO> create(int pageNo, int pageSize) {
        return new Page<>(pageNo, pageSize);
    }
}
