package com.github.thundax.modules.member.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;

import java.util.List;

/**
 * 会员 MyBatis Mapper。
 */
@MyBatisDao
public interface MemberMapper extends CrudDao<MemberDO> {

    List<MemberDO> findByLoginName(String loginName);

    List<MemberDO> findByEmail(String email);

    void updateLoginInfo(MemberDO member);

    void updateInfo(MemberDO member);

    void updateLoginPass(MemberDO member);

    int updateEnableFlag(MemberDO member);

    MemberDO getByZjhm(MemberDO member);

    MemberDO getByYwtbId(MemberDO member);
}
