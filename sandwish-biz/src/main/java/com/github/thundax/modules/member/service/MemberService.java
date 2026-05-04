package com.github.thundax.modules.member.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.query.MemberQuery;
import java.util.List;

/**
 * 会员Service
 */
public interface MemberService {

    Member getById(EntityId id);

    List<Member> batchGetByIds(List<EntityId> ids);

    List<Member> list(MemberQuery query);

    PageDTO<Member> page(MemberQuery query, PageDTO<Member> page);

    void add(Member member);

    void update(Member member);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    int updatePriority(Member member);

    int updatePriority(List<Member> list);

    Member getByLoginName(String loginName);

    Member getByEmail(String email);

    void updateLoginInfo(Member member);

    void updateInfo(Member member);

    void updatePassword(Member member);

    int updateStatus(Member member);

    int updateStatus(List<Member> list);

    Member getByZjhm(MemberQuery query);

    Member getByYwtbId(String ywtbUserId);
}
