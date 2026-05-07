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
        query.setStatus(MemberStatus.ACTIVE);
        query.setName("alice");
        query.setRemarks("remark");

        MemberServiceImpl service = new MemberServiceImpl(dao);
        service.list(query);

        assertEquals("ACTIVE", dao.status);
        assertEquals("alice", dao.name);
        assertEquals("remark", dao.remarks);
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
    public void shouldBatchStatusUpdate() {
        RecordingMemberDao dao = new RecordingMemberDao();
        MemberServiceImpl service = new MemberServiceImpl(dao);

        int count = service.batchUpdateStatus(Arrays.asList(member(8001L), member(8002L)));

        assertEquals(2, count);
        assertEquals(2, dao.statusUpdateCalls);
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
        private String status;
        private String name;
        private String remarks;
        private Member inserted;
        private int statusUpdateCalls;

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
        public List<Member> list(String status, String name, String remarks) {
            this.status = status;
            this.name = name;
            this.remarks = remarks;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Member> page(
                String status, String name, String remarks, int pageNo, int pageSize) {
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        }

        @Override
        public EntityId insert(Member entity) {
            this.inserted = entity;
            return EntityId.of(9801L);
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
        public void updateInfo(Member member) {}

        @Override
        public int updateStatus(Member member) {
            this.statusUpdateCalls++;
            return 1;
        }
    }
}
