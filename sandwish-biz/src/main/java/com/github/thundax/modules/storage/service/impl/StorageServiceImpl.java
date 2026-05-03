package com.github.thundax.modules.storage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.modules.storage.backend.StorageBackendObject;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import com.github.thundax.modules.storage.utils.MetaFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final StorageDao dao;

    public StorageServiceImpl(StorageDao dao) {
        this.dao = dao;
    }

    @Override
    public Storage getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Storage> batchGetByIds(List<EntityId> ids) {
        return dao.batchGetByIds(EntityIdCodec.toValues(ids));
    }

    @Override
    public List<Storage> list(StorageQuery query) {
        return dao.list(
                query == null ? null : query.getMimeType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : ownerTypeValue(query.getOwnerType()),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : query.getBusinessId(),
                query == null ? null : query.getBusinessType(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Page<Storage> page(StorageQuery query, Page<Storage> page) {
        Page<Storage> normalizedPage = normalizePage(page);
        IPage<Storage> dataPage = dao.page(
                query == null ? null : query.getMimeType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : ownerTypeValue(query.getOwnerType()),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : query.getBusinessId(),
                query == null ? null : query.getBusinessType(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Storage storage) {
        storage.setId(EntityIdCodec.toDomain(dao.insert(storage)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Storage storage) {
        dao.update(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    public List<String> listMimeTypes() {
        return dao.listMimeTypes();
    }

    @Override
    public List<String> listBusinessTypes() {
        return dao.listBusinessTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Storage storage) {
        return dao.updateStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVisibility(Storage storage) {
        return dao.updateVisibility(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeBusiness(String businessType, String businessId) {
        return dao.deleteBusinessByBusiness(businessType, businessId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBusiness(List<StorageBusiness> list) {
        dao.insertBusiness(list);
    }

    @Override
    public List<StorageBusiness> listBusiness(Storage entity) {
        return dao.listBusiness(entity);
    }

    @Override
    public boolean canAccess(Storage storage, StorageOwnerType ownerType, String ownerId) {
        if (storage == null) {
            return false;
        }
        if (StorageVisibility.PUBLIC == storage.getVisibility()) {
            return true;
        }
        return StorageVisibility.PRIVATE == storage.getVisibility()
                && storage.getOwnerType() == ownerType
                && StringUtils.isNotBlank(ownerId)
                && StringUtils.equals(storage.getOwnerId(), ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MultipartUploadSession initMultipartUpload(MultipartUploadSession session) {
        if (session == null) {
            throw new BizException("Multipart upload session can not be null");
        }
        Date now = new Date();
        if (StringUtils.isBlank(session.getUploadId())) {
            session.setUploadId(IdGen.uuid());
        }
        session.setUploadStatus(MultipartUploadStatus.INITIATED);
        session.setUploadedPartCount(0);
        session.setCreateDate(now);
        session.setUpdateDate(now);
        session.setId(EntityIdCodec.toDomain(dao.insertMultipartSession(session)));
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MultipartUploadPart uploadMultipartPart(MultipartUploadPart part) {
        if (part == null || StringUtils.isBlank(part.getUploadId())) {
            throw new BizException("Multipart upload part can not be null");
        }
        if (part.getPartNumber() == null || part.getPartNumber() <= 0) {
            throw new BizException("Multipart upload part number must start from 1");
        }
        MultipartUploadSession session = requireActiveMultipartSession(part.getUploadId());
        if (dao.getMultipartPart(part.getUploadId(), part.getPartNumber()) != null) {
            throw new BizException("Multipart upload part already exists: " + part.getPartNumber());
        }

        part.setCreateDate(new Date());
        part.setId(EntityIdCodec.toDomain(dao.insertMultipartPart(part)));

        session.setUploadStatus(MultipartUploadStatus.UPLOADING);
        session.setUploadedPartCount(dao.countMultipartParts(session.getUploadId()));
        session.setUpdateDate(new Date());
        dao.updateMultipartSession(session);
        return part;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Storage completeMultipartUpload(String uploadId, StorageBackendObject object) {
        MultipartUploadSession session = requireActiveMultipartSession(uploadId);
        List<MultipartUploadPart> parts = dao.listMultipartParts(uploadId);
        validateMultipartParts(session, parts);

        Storage storage = toCompletedStorage(session, object);
        storage.setId(EntityIdCodec.toDomain(dao.insert(storage)));

        Date now = new Date();
        session.setUploadStatus(MultipartUploadStatus.COMPLETED);
        session.setUploadedPartCount(parts.size());
        session.setCompletedDate(now);
        session.setUpdateDate(now);
        dao.updateMultipartSession(session);
        return storage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int abortMultipartUpload(String uploadId) {
        MultipartUploadSession session = requireActiveMultipartSession(uploadId);
        Date now = new Date();
        session.setUploadStatus(MultipartUploadStatus.ABORTED);
        session.setAbortedDate(now);
        session.setUpdateDate(now);
        return dao.updateMultipartSession(session);
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private Page<Storage> normalizePage(Page<Storage> page) {
        Page<Storage> normalizedPage = page == null ? new Page<>() : page;
        normalizedPage.initialize();
        return normalizedPage;
    }

    private String ownerTypeValue(StorageOwnerType ownerType) {
        return ownerType == null ? null : ownerType.value();
    }

    private String statusValue(StorageStatus status) {
        return status == null ? null : status.value();
    }

    private String visibilityValue(StorageVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }

    private MultipartUploadSession requireActiveMultipartSession(String uploadId) {
        if (StringUtils.isBlank(uploadId)) {
            throw new BizException("Multipart upload id can not be empty");
        }
        MultipartUploadSession session = dao.getMultipartSessionByUploadId(uploadId);
        if (session == null) {
            throw new BizException("Multipart upload session not found: " + uploadId);
        }
        if (MultipartUploadStatus.COMPLETED == session.getUploadStatus()
                || MultipartUploadStatus.ABORTED == session.getUploadStatus()) {
            throw new BizException("Multipart upload session is closed: " + uploadId);
        }
        return session;
    }

    private void validateMultipartParts(MultipartUploadSession session, List<MultipartUploadPart> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new BizException("Multipart upload has no parts: " + session.getUploadId());
        }
        int expectedPartCount = expectedPartCount(session);
        if (expectedPartCount > 0 && parts.size() != expectedPartCount) {
            throw new BizException("Multipart upload parts are incomplete: " + session.getUploadId());
        }

        List<Integer> partNumbers = new ArrayList<>();
        for (MultipartUploadPart part : parts) {
            partNumbers.add(part.getPartNumber());
        }
        for (int i = 1; i <= expectedPartCount; i++) {
            if (!partNumbers.contains(i)) {
                throw new BizException("Multipart upload part is missing: " + i);
            }
        }
    }

    private int expectedPartCount(MultipartUploadSession session) {
        Long totalSize = session.getTotalSize();
        Long partSize = session.getPartSize();
        if (totalSize == null || totalSize <= 0 || partSize == null || partSize <= 0) {
            return 0;
        }
        return (int) ((totalSize + partSize - 1) / partSize);
    }

    private Storage toCompletedStorage(MultipartUploadSession session, StorageBackendObject object) {
        Storage storage = new Storage();
        storage.setName(baseName(session.getOriginalFilename()));
        storage.setExtendName(extension(session.getOriginalFilename()));
        storage.setMimeType(session.getMimeType());
        storage.setOwnerId(session.getOwnerId());
        storage.setOwnerType(session.getOwnerType());
        storage.setStorageType(object == null ? session.getStorageType() : object.getStorageType());
        storage.setBucketName(object == null ? session.getBucketName() : object.getBucketName());
        storage.setObjectKey(object == null ? session.getObjectKey() : object.getObjectKey());
        storage.setSize(object == null ? session.getTotalSize() : object.getSize());
        storage.setAccessEndpoint(object == null ? null : object.getAccessEndpoint());
        storage.setStatus(StorageStatus.ENABLED);
        storage.setVisibility(StorageVisibility.PRIVATE);
        storage.setCreateDate(new Date());
        return storage;
    }

    private String baseName(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return null;
        }
        int index = originalFilename.lastIndexOf(MetaFile.DOT);
        return index < 0 ? originalFilename : originalFilename.substring(0, index);
    }

    private String extension(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return null;
        }
        int index = originalFilename.lastIndexOf(MetaFile.DOT);
        return index < 0 ? null : StringUtils.lowerCase(originalFilename.substring(index + 1));
    }
}
