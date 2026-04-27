package com.github.thundax.modules.member.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员 MyBatis Mapper。
 */
@MyBatisDao
public interface MemberMapper {

    MemberDO get(MemberDO entity);

    List<MemberDO> getMany(@Param("idList") List<String> idList);

    List<MemberDO> findList(MemberDO entity);

    int insert(MemberDO entity);

    int update(MemberDO entity);

    int updatePriority(MemberDO entity);

    int delete(MemberDO entity);

    List<MemberDO> findByLoginName(String loginName);

    List<MemberDO> findByEmail(String email);

    void updateLoginInfo(MemberDO member);

    void updateInfo(MemberDO member);

    void updateLoginPass(MemberDO member);

    int updateEnableFlag(MemberDO member);

    MemberDO getByZjhm(MemberDO member);

    MemberDO getByYwtbId(MemberDO member);
}
