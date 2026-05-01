package com.github.thundax.modules.member.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.member.entity.Member;
import java.util.List;

/**
 * 会员Service
 */
public interface MemberService {

    Member getById(EntityId id);

    List<Member> batchGetByIds(List<String> ids);

    List<Member> list(Member member);

    Page<Member> page(Member member, Page<Member> page);

    void add(Member member);

    void update(Member member);

    int deleteById(Member member);

    int batchDeleteById(List<Member> list);

    int updatePriority(Member member);

    int updatePriority(List<Member> list);

    /**
     * 根据 loginName 获取
     *
     * @param loginName loginName
     * @return 对象
     */
    Member getByLoginName(String loginName);

    /**
     * 根据 email 获取
     *
     * @param email email
     * @return 对象
     */
    Member getByEmail(String email);

    /**
     * 更新登录信息
     *
     * @param member 对象
     */
    void updateLoginInfo(Member member);

    /**
     * 更新信息
     *
     * @param member 对象
     */
    void updateInfo(Member member);

    /**
     * 更新密码
     *
     * @param member 对象
     */
    void updatePassword(Member member);

    /**
     * 启用/禁用
     *
     * @param member 对象
     * @return 影响记录数
     */
    int updateStatus(Member member);

    /**
     * 启用/禁用
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updateStatus(List<Member> list);

    Member getByZjhm(Member member);

    Member getByYwtbId(String ywtbUserId);
}
