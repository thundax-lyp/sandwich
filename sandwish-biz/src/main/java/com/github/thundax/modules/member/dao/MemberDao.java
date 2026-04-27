package com.github.thundax.modules.member.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.member.entity.Member;
import java.util.List;

/** @author wdit */
public interface MemberDao extends CrudDao<Member> {

    /**
     * 根据 loginName 获取
     *
     * @param loginName loginName
     * @return 列表
     */
    List<Member> findByLoginName(String loginName);

    /**
     * 根据 email 获取
     *
     * @param email email
     * @return 列表
     */
    List<Member> findByEmail(String email);

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
    void updateLoginPass(Member member);

    /**
     * 启用/禁用
     *
     * @param member 对象
     * @return 影响记录数
     */
    int updateEnableFlag(Member member);

    /** 根据证件号码查询 */
    Member getByZjhm(Member member);

    /** 根据一网通办的id查询 */
    Member getByYwtbId(Member member);
}
