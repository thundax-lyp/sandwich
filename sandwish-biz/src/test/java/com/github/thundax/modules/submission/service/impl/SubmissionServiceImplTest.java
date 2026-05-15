package com.github.thundax.modules.submission.service.impl;

import static org.junit.Assert.*;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.submission.dao.SubmissionDao;
import com.github.thundax.modules.submission.dao.SubmissionImageDao;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.command.ChangeSubmissionStatusCommand;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import com.github.thundax.modules.submission.service.command.SubmissionSortCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class SubmissionServiceImplTest {

    @Test
    public void shouldCreateSubmissionWithImages() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        RecordingSubmissionImageDao imageDao = new RecordingSubmissionImageDao();
        dao.maxPriority = 20;
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, imageDao);

        SubmissionId id = service.create(new CreateSubmissionCommand(
                "title", "content", Arrays.asList(StoredObjectId.of(11L), StoredObjectId.of(12L)), "client-1"));

        assertEquals(SubmissionId.of(9001L), id);
        assertEquals(SubmissionStatus.SUBMITTED, dao.inserted.getStatus());
        assertEquals(30, dao.inserted.getPriority());
        assertNotNull(dao.inserted.getSubmittedAt());
        assertEquals(2, imageDao.insertedImages.size());
        assertEquals(0, imageDao.insertedImages.get(0).getSortOrder());
        assertEquals(StoredObjectId.of(12L), imageDao.insertedImages.get(1).getStorageObjectId());
    }

    @Test
    public void shouldExpandQueryForPage() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao());
        SubmissionQuery query = new SubmissionQuery();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setStatus(SubmissionStatus.APPROVED);
        query.setSourceClientId("client-1");
        query.setSubmittedAtBegin(begin);
        query.setSubmittedAtEnd(end);

        service.page(query, new com.github.thundax.common.page.PageQuery(2, 20));

        assertEquals("APPROVED", dao.status);
        assertEquals("client-1", dao.sourceClientId);
        assertSame(begin, dao.submittedAtBegin);
        assertSame(end, dao.submittedAtEnd);
        assertEquals(2, dao.pageNo);
        assertEquals(20, dao.pageSize);
    }

    @Test
    public void shouldChangeStatus() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao());

        int count = service.changeStatus(
                new ChangeSubmissionStatusCommand(SubmissionId.of(9001L), SubmissionStatus.REJECTED));

        assertEquals(1, count);
        assertEquals(SubmissionStatus.REJECTED, dao.statusEntity.getStatus());
        assertNotNull(dao.statusEntity.getLastStatusChangedAt());
    }

    @Test
    public void shouldExchangeSubmissionPriority() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        dao.currentList = Arrays.asList(submission(1L, 10), submission(2L, 20), submission(3L, 30));
        dao.maxPriority = 30;
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao());

        service.sort(new SubmissionSortCommand(
                Arrays.asList(SubmissionId.of(2L), SubmissionId.of(1L), SubmissionId.of(3L)), SortDirection.ASC));

        assertEquals(Arrays.asList("2:40", "1:20", "2:10"), dao.priorityUpdates);
    }

    @Test(expected = BizException.class)
    public void shouldRejectDuplicateSortIds() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        dao.currentList = Arrays.asList(submission(1L, 10), submission(2L, 20));
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao());

        service.sort(
                new SubmissionSortCommand(Arrays.asList(SubmissionId.of(1L), SubmissionId.of(1L)), SortDirection.ASC));
    }

    private static Submission submission(Long id, int priority) {
        Submission submission = new Submission();
        submission.setId(SubmissionIdCodec.toDomain(id));
        submission.setPriority(priority);
        return submission;
    }

    private static class RecordingSubmissionDao implements SubmissionDao {

        private int maxPriority;
        private Submission inserted;
        private Submission statusEntity;
        private String status;
        private String sourceClientId;
        private Date submittedAtBegin;
        private Date submittedAtEnd;
        private int pageNo;
        private int pageSize;
        private List<Submission> currentList;
        private List<String> priorityUpdates = new java.util.ArrayList<>();

        @Override
        public Submission getById(SubmissionId id) {
            return submission(id.value(), 10);
        }

        @Override
        public List<Submission> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<Submission> list(
                String status,
                String sourceClientId,
                Date submittedAtBegin,
                Date submittedAtEnd,
                SortDirection sortDirection) {
            return currentList;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Submission> page(
                String status,
                String sourceClientId,
                Date submittedAtBegin,
                Date submittedAtEnd,
                SortDirection sortDirection,
                int pageNo,
                int pageSize) {
            this.status = status;
            this.sourceClientId = sourceClientId;
            this.submittedAtBegin = submittedAtBegin;
            this.submittedAtEnd = submittedAtEnd;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        }

        @Override
        public int maxPriority() {
            return maxPriority;
        }

        @Override
        public SubmissionId insert(Submission entity) {
            this.inserted = entity;
            return SubmissionId.of(9001L);
        }

        @Override
        public int updateStatus(Submission entity) {
            this.statusEntity = entity;
            return 1;
        }

        @Override
        public int updatePriority(SubmissionId id, int priority) {
            priorityUpdates.add(id.value() + ":" + priority);
            return 1;
        }
    }

    private static class RecordingSubmissionImageDao implements SubmissionImageDao {

        private List<SubmissionImage> insertedImages;

        @Override
        public void batchInsert(List<SubmissionImage> images) {
            this.insertedImages = images;
        }

        @Override
        public List<SubmissionImage> listBySubmissionId(SubmissionId submissionId) {
            return java.util.Collections.emptyList();
        }
    }
}
