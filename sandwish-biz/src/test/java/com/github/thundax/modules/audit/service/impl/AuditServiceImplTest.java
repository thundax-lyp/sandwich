package com.github.thundax.modules.audit.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.audit.dao.AuditLogDao;
import com.github.thundax.modules.audit.dao.AuditMetaDao;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.audit.entity.valueobject.AuditLogId;
import com.github.thundax.modules.audit.entity.valueobject.AuditMetaId;
import com.github.thundax.modules.audit.entity.valueobject.AuditObjectRef;
import com.github.thundax.modules.audit.runtime.AuditDiffService;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.audit.service.command.CreateAuditLogCommand;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class AuditServiceImplTest {

    @Test
    public void shouldInsertMetaAndLogForNewObject() {
        RecordingAuditMetaDao metaDao = new RecordingAuditMetaDao();
        RecordingAuditLogDao logDao = new RecordingAuditLogDao();
        AuditServiceImpl service = new AuditServiceImpl(metaDao, logDao, new AuditDiffService());
        CreateAuditLogCommand command = new CreateAuditLogCommand();
        command.setObjectType("User");
        command.setObjectId("1001");
        command.setAction(AuditAction.CREATE);
        command.setAfterSnapshot(AuditSnapshots.of("User", "1001", "user", AuditSnapshots.field("name", "名称", "user")));
        command.setRecordWhenUnchanged(true);

        AuditLogId id = service.record(command);

        assertEquals(AuditLogId.of(9002L), id);
        assertEquals("User", metaDao.inserted.getObjectType());
        assertEquals(AuditAction.CREATE, metaDao.inserted.getLastAction());
        assertEquals(logDao.inserted.getOperatorType(), metaDao.inserted.getLastOperatorType());
        assertEquals(logDao.inserted.getOccurredAt(), metaDao.inserted.getLastOperatedAt());
        assertEquals(AuditMetaId.of(9001L), logDao.inserted.getMetaId());
        assertEquals(1L, logDao.inserted.getVersion().longValue());
        assertEquals(AuditLogId.of(9002L), metaDao.updated.getLastLogId());
    }

    @Test
    public void shouldSkipInvalidCommand() {
        AuditServiceImpl service =
                new AuditServiceImpl(new RecordingAuditMetaDao(), new RecordingAuditLogDao(), new AuditDiffService());

        assertNull(service.record(null));
    }

    private static class RecordingAuditMetaDao implements AuditMetaDao {

        private AuditMeta inserted;
        private AuditMeta updated;

        @Override
        public AuditMeta getByObjectRef(AuditObjectRef objectRef) {
            return null;
        }

        @Override
        public AuditMetaId insert(AuditMeta meta) {
            this.inserted = copy(meta);
            return AuditMetaId.of(9001L);
        }

        @Override
        public int update(AuditMeta meta) {
            this.updated = copy(meta);
            return 1;
        }

        private AuditMeta copy(AuditMeta meta) {
            AuditMeta copied = new AuditMeta();
            copied.setId(meta.getId());
            copied.setObjectType(meta.getObjectType());
            copied.setObjectId(meta.getObjectId());
            copied.setVersion(meta.getVersion());
            copied.setLastLogId(meta.getLastLogId());
            copied.setLastAction(meta.getLastAction());
            copied.setLastOperatorType(meta.getLastOperatorType());
            copied.setLastOperatorId(meta.getLastOperatorId());
            copied.setLastOperatorName(meta.getLastOperatorName());
            copied.setLastOperatedAt(meta.getLastOperatedAt());
            copied.setCreatedLogId(meta.getCreatedLogId());
            copied.setCreatedAt(meta.getCreatedAt());
            return copied;
        }
    }

    private static class RecordingAuditLogDao implements AuditLogDao {

        private AuditLog inserted;

        @Override
        public AuditLogId insert(AuditLog log) {
            this.inserted = log;
            return AuditLogId.of(9002L);
        }

        @Override
        public AuditLog getByIdempotencyKey(String idempotencyKey) {
            return null;
        }

        @Override
        public AuditLog getById(AuditLogId id) {
            return null;
        }

        @Override
        public List<AuditLog> listByObject(String objectType, String objectId) {
            return new ArrayList<>();
        }

        @Override
        public Page<AuditLog> page(
                String objectType,
                String objectId,
                AuditAction action,
                com.github.thundax.modules.audit.entity.enums.AuditOperatorType operatorType,
                String operatorId,
                String source,
                String requestId,
                Date beginDate,
                Date endDate,
                int pageNo,
                int pageSize) {
            return new Page<>();
        }
    }
}
