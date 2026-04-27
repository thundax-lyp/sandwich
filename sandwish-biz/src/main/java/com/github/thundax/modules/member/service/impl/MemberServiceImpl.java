package com.github.thundax.modules.member.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author wdit */
@Service
@Transactional(readOnly = true)
public class MemberServiceImpl extends CrudServiceImpl<MemberDao, Member> implements MemberService {

    @Autowired
    public MemberServiceImpl(MemberDao dao) {
        super(dao);
    }

    @Override
    public Member getByLoginName(String loginName) {
        return findOne(() -> dao.findByLoginName(loginName));
    }

    @Override
    public Member getByEmail(String email) {
        return findOne(() -> dao.findByEmail(email));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Member member) {
        dao.updateLoginInfo(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(Member member) {
        dao.updateInfo(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(Member member) {
        return dao.updateEnableFlag(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(List<Member> list) {
        return batchOperate(list, this::updateEnableFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member getByZjhm(Member member) {
        return dao.getByZjhm(member);
    }

    @Override
    public Member getByYwtbId(String ywtbUserId) {
        if (StringUtils.isEmpty(ywtbUserId)) {
            return null;
        }
        Member member = new Member();
        member.setYwtbId(ywtbUserId);
        return dao.getByYwtbId(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Member member) {
        member.preUpdate();
        dao.updateLoginPass(member);
    }
}
