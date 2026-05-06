package com.github.thundax.modules.member.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.Member;
import java.util.Date;
import java.util.List;

public interface MemberDao {

    Member getById(EntityId id);

    List<Member> listByIds(List<String> idList);

    List<Member> list(
            String enableFlag,
            String email,
            String name,
            String remarks,
            Date beginRegisterDate,
            Date endRegisterDate,
            Date beginLoginDate,
            Date endLoginDate,
            String mobile);

    Page<Member> page(
            String enableFlag,
            String email,
            String name,
            String remarks,
            Date beginRegisterDate,
            Date endRegisterDate,
            Date beginLoginDate,
            Date endLoginDate,
            String mobile,
            int pageNo,
            int pageSize);

    String insert(Member entity);

    int update(Member entity);

    int updatePriority(Member entity);

    int deleteById(EntityId id);

    List<Member> listByLoginName(String loginName);

    List<Member> listByEmail(String email);

    void updateLoginInfo(Member member);

    void updateInfo(Member member);

    void updateLoginPass(Member member);

    int updateStatus(Member member);
}
