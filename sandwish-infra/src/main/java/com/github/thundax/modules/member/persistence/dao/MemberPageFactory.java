package com.github.thundax.modules.member.persistence.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;

final class MemberPageFactory {

    private MemberPageFactory() {}

    static IPage<MemberDO> create(int pageNo, int pageSize) {
        return new Page<>(pageNo, pageSize);
    }
}
