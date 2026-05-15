package com.github.thundax.modules.submission.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.AddStorageReferencesCommand;
import com.github.thundax.modules.storage.service.command.RemoveStorageReferencesCommand;
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
import org.mockito.ArgumentCaptor;

public class SubmissionServiceImplTest {

    @Test
    public void shouldCreateSubmissionWithImages() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        RecordingSubmissionImageDao imageDao = new RecordingSubmissionImageDao();
        StorageService storageService = mock(StorageService.class);
        dao.maxPriority = 20;
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, imageDao, storageService);

        SubmissionId id = service.create(new CreateSubmissionCommand(
                "title", "content", Arrays.asList(StoredObjectId.of(11L), StoredObjectId.of(12L))));

        assertEquals(SubmissionId.of(9001L), id);
        assertEquals(SubmissionStatus.SUBMITTED, dao.inserted.getStatus());
        assertEquals(30, dao.inserted.getPriority());
        assertNotNull(dao.inserted.getSubmittedAt());
        assertEquals(2, imageDao.insertedImages.size());
        assertEquals(0, imageDao.insertedImages.get(0).getSortOrder());
        assertEquals(StoredObjectId.of(12L), imageDao.insertedImages.get(1).getStorageObjectId());
        ArgumentCaptor<AddStorageReferencesCommand> captor = ArgumentCaptor.forClass(AddStorageReferencesCommand.class);
        verify(storageService).addReferences(captor.capture());
        assertEquals(2, captor.getValue().getReferences().size());
        assertEquals(
                StorageOwnerType.SUBMISSION,
                captor.getValue().getReferences().get(0).getOwnerType());
        assertEquals("9001", captor.getValue().getReferences().get(0).getOwnerId());
    }

    @Test
    public void shouldExpandQueryForPage() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        SubmissionServiceImpl service =
                new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao(), mock(StorageService.class));
        SubmissionQuery query = new SubmissionQuery();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setStatus(SubmissionStatus.APPROVED);
        query.setSubmittedAtBegin(begin);
        query.setSubmittedAtEnd(end);

        service.page(query, new com.github.thundax.common.page.PageQuery(2, 20));

        assertEquals("APPROVED", dao.status);
        assertSame(begin, dao.submittedAtBegin);
        assertSame(end, dao.submittedAtEnd);
        assertEquals(2, dao.pageNo);
        assertEquals(20, dao.pageSize);
    }

    @Test
    public void shouldChangeStatus() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        SubmissionServiceImpl service =
                new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao(), mock(StorageService.class));

        int count = service.changeStatus(
                new ChangeSubmissionStatusCommand(SubmissionId.of(9001L), SubmissionStatus.REJECTED));

        assertEquals(1, count);
        assertEquals(SubmissionStatus.REJECTED, dao.statusEntity.getStatus());
    }

    @Test
    public void shouldExchangeSubmissionPriority() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        dao.currentList = Arrays.asList(submission(1L, 10), submission(2L, 20), submission(3L, 30));
        dao.maxPriority = 30;
        SubmissionServiceImpl service =
                new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao(), mock(StorageService.class));

        service.sort(new SubmissionSortCommand(
                Arrays.asList(SubmissionId.of(2L), SubmissionId.of(1L), SubmissionId.of(3L)), SortDirection.ASC));

        assertEquals(Arrays.asList("2:40", "1:20", "2:10"), dao.priorityUpdates);
    }

    @Test(expected = BizException.class)
    public void shouldRejectDuplicateSortIds() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        dao.currentList = Arrays.asList(submission(1L, 10), submission(2L, 20));
        SubmissionServiceImpl service =
                new SubmissionServiceImpl(dao, new RecordingSubmissionImageDao(), mock(StorageService.class));

        service.sort(
                new SubmissionSortCommand(Arrays.asList(SubmissionId.of(1L), SubmissionId.of(1L)), SortDirection.ASC));
    }

    @Test
    public void shouldRemoveSubmissionAndUnbindStorageReferencesOnly() {
        RecordingSubmissionDao dao = new RecordingSubmissionDao();
        RecordingSubmissionImageDao imageDao = new RecordingSubmissionImageDao();
        StorageService storageService = mock(StorageService.class);
        SubmissionServiceImpl service = new SubmissionServiceImpl(dao, imageDao, storageService);

        int count = service.remove(SubmissionId.of(9001L));

        assertEquals(1, count);
        assertEquals(SubmissionId.of(9001L), imageDao.deletedSubmissionId);
        assertEquals(SubmissionId.of(9001L), dao.deletedId);
        ArgumentCaptor<RemoveStorageReferencesCommand> captor =
                ArgumentCaptor.forClass(RemoveStorageReferencesCommand.class);
        verify(storageService).removeReferences(captor.capture());
        assertEquals(StorageOwnerType.SUBMISSION, captor.getValue().getOwnerType());
        assertEquals("9001", captor.getValue().getOwnerId());
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
        private Date submittedAtBegin;
        private Date submittedAtEnd;
        private int pageNo;
        private int pageSize;
        private List<Submission> currentList;
        private List<String> priorityUpdates = new java.util.ArrayList<>();
        private SubmissionId deletedId;

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
                String status, Date submittedAtBegin, Date submittedAtEnd, SortDirection sortDirection) {
            return currentList;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Submission> page(
                String status,
                Date submittedAtBegin,
                Date submittedAtEnd,
                SortDirection sortDirection,
                int pageNo,
                int pageSize) {
            this.status = status;
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

        @Override
        public int deleteById(SubmissionId id) {
            this.deletedId = id;
            return 1;
        }
    }

    private static class RecordingSubmissionImageDao implements SubmissionImageDao {

        private List<SubmissionImage> insertedImages;
        private SubmissionId deletedSubmissionId;

        @Override
        public void batchInsert(List<SubmissionImage> images) {
            this.insertedImages = images;
        }

        @Override
        public List<SubmissionImage> listBySubmissionId(SubmissionId submissionId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public int deleteBySubmissionId(SubmissionId submissionId) {
            this.deletedSubmissionId = submissionId;
            return 2;
        }
    }
}
