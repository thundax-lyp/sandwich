package com.github.thundax.modules.member.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.query.MemberQuery;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class MemberServiceImplTest {

    @Test
    public void shouldGetMemberById() {
        RecordingMemberDao dao = new RecordingMemberDao();
        Member expected = member(8001L);
        dao.getResult = expected;

        MemberServiceImpl service = new MemberServiceImpl(dao);

        assertSame(expected, service.getById(EntityId.of(8001L)));
        assertEquals(Long.valueOf(8001L), dao.id);
    }

    @Test
    public void shouldIgnoreBlankId() {
        RecordingMemberDao dao = new RecordingMemberDao();
        MemberServiceImpl service = new MemberServiceImpl(dao);

        assertEquals(null, service.getById((EntityId) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingMemberDao dao = new RecordingMemberDao();
        MemberQuery query = new MemberQuery();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setStatus(MemberStatus.ENABLED);
        query.setEmail("a@example.com");
        query.setName("alice");
        query.setRemarks("remark");
        query.setBeginRegisterDate(begin);
        query.setEndRegisterDate(end);
        query.setMobile("13800000000");

        MemberServiceImpl service = new MemberServiceImpl(dao);
        service.list(query);

        assertEquals("ENABLED", dao.enableFlag);
        assertEquals("a@example.com", dao.email);
        assertEquals("alice", dao.name);
        assertEquals("remark", dao.remarks);
        assertEquals(query.getBeginRegisterDate(), dao.beginRegisterDate);
        assertEquals(query.getEndRegisterDate(), dao.endRegisterDate);
        assertEquals("13800000000", dao.mobile);
    }

    @Test
    public void shouldPrepareEntityBeforeSave() {
        RecordingMemberDao dao = new RecordingMemberDao();
        Member member = new Member();

        MemberServiceImpl service = new MemberServiceImpl(dao);
        service.add(member);

        assertNotNull(member.getId());
        assertEquals(null, member.getCreateDate());
        assertEquals(null, member.getUpdateDate());
        assertSame(member, dao.inserted);
    }

    @Test
    public void shouldBatchEnableFlagUpdate() {
        RecordingMemberDao dao = new RecordingMemberDao();
        MemberServiceImpl service = new MemberServiceImpl(dao);

        int count = service.batchUpdateStatus(Arrays.asList(member(8001L), member(8002L)));

        assertEquals(2, count);
        assertEquals(2, dao.enableUpdateCalls);
    }

    private static Member member(Long id) {
        Member member = new Member();
        member.setId(EntityIdCodec.toDomain(id));
        return member;
    }

    private static class RecordingMemberDao implements MemberDao {

        private Member getResult;
        private Long id;
        private int getCalls;
        private String enableFlag;
        private String email;
        private String name;
        private String remarks;
        private Date beginRegisterDate;
        private Date endRegisterDate;
        private String mobile;
        private Member inserted;
        private int enableUpdateCalls;

        @Override
        public Member getById(EntityId id) {
            this.getCalls++;
            this.id = id.value();
            return getResult;
        }

        @Override
        public List<Member> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<Member> list(
                String enableFlag,
                String email,
                String name,
                String remarks,
                Date beginRegisterDate,
                Date endRegisterDate,
                Date beginLoginDate,
                Date endLoginDate,
                String mobile) {
            this.enableFlag = enableFlag;
            this.email = email;
            this.name = name;
            this.remarks = remarks;
            this.beginRegisterDate = beginRegisterDate;
            this.endRegisterDate = endRegisterDate;
            this.mobile = mobile;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Member> page(
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
                int pageSize) {
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        }

        @Override
        public String insert(Member entity) {
            this.inserted = entity;
            return "9801";
        }

        @Override
        public int update(Member entity) {
            return 1;
        }

        @Override
        public int updatePriority(Member entity) {
            return 1;
        }

        @Override
        public int deleteById(EntityId id) {
            return 1;
        }

        @Override
        public List<Member> listByLoginName(String loginName) {
            return null;
        }

        @Override
        public List<Member> listByEmail(String email) {
            return null;
        }

        @Override
        public void updateLoginInfo(Member member) {}

        @Override
        public void updateInfo(Member member) {}

        @Override
        public void updateLoginPass(Member member) {}

        @Override
        public int updateStatus(Member member) {
            this.enableUpdateCalls++;
            return 1;
        }
    }
}
