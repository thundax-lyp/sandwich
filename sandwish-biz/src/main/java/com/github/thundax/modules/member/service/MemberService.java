package com.github.thundax.modules.member.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.member.entity.Member;

import java.util.List;

/**
 * 会员Service
 *
 * @author wdit
 */
public interface MemberService extends CrudService<Member> {

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
    int updateEnableFlag(Member member);

    /**
     * 启用/禁用
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updateEnableFlag(List<Member> list);

    /**
     * 根据证件号码查询
     */
    Member getByZjhm(Member member);

    /**
     * 根据一网通办的id查询
     */
    Member getByYwtbId(String ywtbUserId);
}
