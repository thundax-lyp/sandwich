package com.github.thundax.modules.member.service;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import com.github.thundax.modules.member.service.command.MemberCommand;
import com.github.thundax.modules.member.service.query.MemberQuery;
import java.util.List;

/**
 * 会员Service
 */
public interface MemberService {

    Member get(MemberId id);

    List<Member> list(MemberQuery query);

    PageResult<Member> page(MemberQuery query, PageQuery page);

    MemberId create(MemberCommand command);

    void change(MemberCommand command);

    void changeInfo(MemberCommand command);

    int changeStatus(MemberCommand command);

    int remove(MemberCommand command);

    void sort(List<MemberId> orderedIds, SortDirection sortDirection) throws ApiException;
}
