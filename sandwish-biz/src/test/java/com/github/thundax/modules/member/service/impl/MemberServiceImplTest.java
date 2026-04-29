package com.github.thundax.modules.member.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class MemberServiceImplTest {

    @Test
    public void shouldGetMemberById() {
        RecordingMemberDao dao = new RecordingMemberDao();
        Member expected = new Member("m1");
        dao.getResult = expected;

        MemberServiceImpl service = new MemberServiceImpl(dao);

        assertSame(expected, service.get(EntityId.of("m1")));
        assertEquals("m1", dao.id);
    }

    @Test
    public void shouldIgnoreBlankId() {
        RecordingMemberDao dao = new RecordingMemberDao();
        MemberServiceImpl service = new MemberServiceImpl(dao);

        assertEquals(null, service.get((EntityId) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingMemberDao dao = new RecordingMemberDao();
        Member query = new Member();
        Member.Query condition = new Member.Query();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        condition.setEnableFlag("1");
        condition.setEmail("a@example.com");
        condition.setName("alice");
        condition.setRemarks("remark");
        condition.setBeginRegisterDate(begin);
        condition.setEndRegisterDate(end);
        condition.setMobile("13800000000");
        query.setQuery(condition);

        MemberServiceImpl service = new MemberServiceImpl(dao);
        service.findList(query);

        assertEquals("1", dao.enableFlag);
        assertEquals("a@example.com", dao.email);
        assertEquals("alice", dao.name);
        assertEquals("remark", dao.remarks);
        assertEquals(condition.getBeginRegisterDate(), dao.beginRegisterDate);
        assertEquals(condition.getEndRegisterDate(), dao.endRegisterDate);
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

        int count = service.updateEnableFlag(Arrays.asList(new Member("m1"), new Member("m2")));

        assertEquals(2, count);
        assertEquals(2, dao.enableUpdateCalls);
    }

    @Test
    public void shouldExpandYwtbAndZjhmQueries() {
        RecordingMemberDao dao = new RecordingMemberDao();
        Member query = new Member();
        Member.Query condition = new Member.Query();
        condition.setZjhm("310000");
        query.setQuery(condition);

        MemberServiceImpl service = new MemberServiceImpl(dao);
        service.getByZjhm(query);
        service.getByYwtbId("ywtb-1");

        assertEquals("310000", dao.zjhm);
        assertEquals("ywtb-1", dao.ywtbId);
    }

    private static class RecordingMemberDao implements MemberDao {

        private Member getResult;
        private String id;
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
        private String zjhm;
        private String ywtbId;

        @Override
        public Member get(EntityId id) {
            this.getCalls++;
            this.id = id.value();
            return getResult;
        }

        @Override
        public List<Member> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<Member> findList(
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
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Member> findPage(
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
            return "generated-member-id";
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
        public int delete(EntityId id) {
            return 1;
        }

        @Override
        public List<Member> findByLoginName(String loginName) {
            return null;
        }

        @Override
        public List<Member> findByEmail(String email) {
            return null;
        }

        @Override
        public void updateLoginInfo(Member member) {}

        @Override
        public void updateInfo(Member member) {}

        @Override
        public void updateLoginPass(Member member) {}

        @Override
        public int updateEnableFlag(Member member) {
            this.enableUpdateCalls++;
            return 1;
        }

        @Override
        public Member getByZjhm(String zjhm) {
            this.zjhm = zjhm;
            return null;
        }

        @Override
        public Member getByYwtbId(String ywtbId) {
            this.ywtbId = ywtbId;
            return null;
        }
    }
}
