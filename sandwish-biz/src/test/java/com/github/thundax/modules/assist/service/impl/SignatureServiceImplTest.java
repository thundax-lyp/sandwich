package com.github.thundax.modules.assist.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.dao.SignatureDao;
import com.github.thundax.modules.assist.entity.Signature;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SignatureServiceImplTest {

    @Test
    public void shouldFindByBusinessKey() {
        RecordingSignatureDao dao = new RecordingSignatureDao();
        Signature expected = new Signature(null, "User", "u1");
        dao.findResult = expected;

        SignatureServiceImpl service = new SignatureServiceImpl(dao);

        assertSame(expected, service.find("User", "u1"));
        assertEquals("User", dao.businessType);
        assertEquals("u1", dao.businessId);
    }

    @Test
    public void shouldIgnoreBlankFindCondition() {
        RecordingSignatureDao dao = new RecordingSignatureDao();
        SignatureServiceImpl service = new SignatureServiceImpl(dao);

        assertEquals(null, service.find("", "u1"));
        assertEquals(0, dao.findCalls);
    }

    @Test
    public void shouldPageByExpandedBusinessType() {
        RecordingSignatureDao dao = new RecordingSignatureDao();
        Page<Signature> page = new Page<>(2, 20);
        dao.pageResult = page;

        SignatureServiceImpl service = new SignatureServiceImpl(dao);

        assertSame(page, service.findPage("Log", page));
        assertEquals("Log", dao.pageBusinessType);
        assertEquals(page.getPageNo(), dao.pageNo);
        assertEquals(page.getPageSize(), dao.pageSize);
    }

    @Test
    public void shouldPrepareEntityBeforeSave() {
        RecordingSignatureDao dao = new RecordingSignatureDao();
        Signature signature = new Signature(null, "Menu", "m1");

        SignatureServiceImpl service = new SignatureServiceImpl(dao);
        service.add(signature);

        assertNotNull(signature.getId());
        assertNotNull(signature.getCreateDate());
        assertNotNull(signature.getUpdateDate());
        assertSame(signature, dao.insertedEntity);
        assertEquals(null, dao.updatedEntity);
    }

    @Test
    public void shouldPrepareEntityBeforeUpdate() {
        RecordingSignatureDao dao = new RecordingSignatureDao();
        Signature signature = new Signature("s1", "Menu", "m1");

        SignatureServiceImpl service = new SignatureServiceImpl(dao);
        service.update(signature);

        assertEquals("s1", signature.getId());
        assertNotNull(signature.getUpdateDate());
        assertEquals(null, signature.getCreateDate());
        assertSame(signature, dao.updatedEntity);
        assertEquals(null, dao.insertedEntity);
    }

    @Test
    public void shouldDeleteEachBusinessKey() {
        RecordingSignatureDao dao = new RecordingSignatureDao();
        SignatureServiceImpl service = new SignatureServiceImpl(dao);
        List<Signature> signatures =
                Arrays.asList(new Signature(null, "User", "u1"), new Signature(null, "Role", "r1"));

        int count = service.delete(signatures);

        assertEquals(2, count);
        assertEquals(Arrays.asList("User:u1", "Role:r1"), dao.deletedBusinessKeys);
    }

    private static class RecordingSignatureDao implements SignatureDao {

        private Signature findResult;
        private Page<Signature> pageResult;
        private String businessType;
        private String businessId;
        private int findCalls;
        private String pageBusinessType;
        private int pageNo;
        private int pageSize;
        private Signature insertedEntity;
        private Signature updatedEntity;
        private List<String> deletedBusinessKeys = new java.util.ArrayList<>();

        @Override
        public Signature find(String businessType, String businessId) {
            this.findCalls++;
            this.businessType = businessType;
            this.businessId = businessId;
            return findResult;
        }

        @Override
        public List<Signature> findByBusinessIds(List<String> businessIdList) {
            return null;
        }

        @Override
        public List<Signature> findList(
                String businessType, String businessId, List<String> businessIdList, String isVerifySign) {
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Signature> findPage(
                String businessType, int pageNo, int pageSize) {
            this.pageBusinessType = businessType;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Signature> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                            pageResult.getPageNo(), pageResult.getPageSize());
            dataPage.setTotal(pageResult.getCount());
            dataPage.setRecords(pageResult.getList());
            return dataPage;
        }

        @Override
        public String insert(Signature entity) {
            this.insertedEntity = entity;
            return entity.getBusinessId();
        }

        @Override
        public int update(Signature entity) {
            this.updatedEntity = entity;
            return 1;
        }

        @Override
        public int delete(String businessType, String businessId) {
            deletedBusinessKeys.add(businessType + ":" + businessId);
            return 1;
        }
    }
}
