package com.github.thundax.modules.member.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.Member;
import java.util.List;

public interface MemberDao {

    Member getById(EntityId id);

    List<Member> listByIds(List<Long> idList);

    List<Member> list(String status, String name, String remarks);

    Page<Member> page(String status, String name, String remarks, int pageNo, int pageSize);

    EntityId insert(Member entity);

    int update(Member entity);

    int updatePriority(Member entity);

    int deleteById(EntityId id);

    void updateInfo(Member member);

    int updateStatus(Member member);
}
