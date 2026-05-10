package com.github.thundax.modules.member.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import java.util.List;

public interface MemberDao {

    Member getById(MemberId id);

    List<Member> listByIds(List<Long> idList);

    List<Member> list(String status, String name, String remarks, SortDirection sortDirection);

    Page<Member> page(
            String status, String name, String remarks, SortDirection sortDirection, int pageNo, int pageSize);

    int maxPriority();

    MemberId insert(Member entity);

    int update(Member entity);

    int updatePriority(MemberId id, int priority);

    int deleteById(MemberId id);

    void updateInfo(Member member);

    int updateStatus(Member member);
}
