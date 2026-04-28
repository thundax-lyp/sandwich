package com.github.thundax.modules.member.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.member.entity.Member;
import java.util.Date;
import java.util.List;

public interface MemberDao {

    Member get(String id);

    List<Member> getMany(List<String> idList);

    List<Member> findList(
            String enableFlag,
            String email,
            String name,
            String remarks,
            Date beginRegisterDate,
            Date endRegisterDate,
            Date beginLoginDate,
            Date endLoginDate,
            String mobile);

    Page<Member> findPage(
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

    int delete(String id);

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


    Member getByZjhm(String zjhm);


    Member getByYwtbId(String ywtbId);
}
