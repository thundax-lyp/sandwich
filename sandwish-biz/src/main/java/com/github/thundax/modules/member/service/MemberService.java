package com.github.thundax.modules.member.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.query.MemberQuery;
import java.util.List;

/**
 * 会员Service
 */
public interface MemberService {

    Member getById(EntityId id);

    List<Member> listByIds(List<EntityId> ids);

    List<Member> list(MemberQuery query);

    PageResult<Member> page(MemberQuery query, PageQuery page);

    EntityId add(Member member);

    void update(Member member);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    void updateInfo(Member member);

    int updateStatus(Member member);

    int batchUpdateStatus(List<Member> list);
}
