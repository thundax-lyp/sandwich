package com.github.thundax.modules.storage.service.impl;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.modules.storage.dao.MultipartUploadDao;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.service.MultipartUploadService;
import com.github.thundax.modules.storage.utils.MetaFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MultipartUploadServiceImpl implements MultipartUploadService {

    private final MultipartUploadDao multipartUploadDao;
    private final StoredObjectDao storedObjectDao;

    public MultipartUploadServiceImpl(MultipartUploadDao multipartUploadDao, StoredObjectDao storedObjectDao) {
        this.multipartUploadDao = multipartUploadDao;
        this.storedObjectDao = storedObjectDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MultipartUploadSession initMultipartUpload(MultipartUploadSession session) {
        if (session == null) {
            throw new BizException("Multipart upload session can not be null");
        }
        Date now = new Date();
        if (StringUtils.isBlank(session.getUploadId())) {
            session.setUploadId(UuidHelper.compact());
        }
        session.setUploadStatus(MultipartUploadStatus.INITIATED);
        session.setUploadedPartCount(0);
        session.setCreateDate(now);
        session.setUpdateDate(now);
        session.setId(multipartUploadDao.insertMultipartSession(session));
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
        if (multipartUploadDao.getMultipartPart(part.getUploadId(), part.getPartNumber()) != null) {
            throw new BizException("Multipart upload part already exists: " + part.getPartNumber());
        }

        part.setCreateDate(new Date());
        part.setId(multipartUploadDao.insertMultipartPart(part));

        session.setUploadStatus(MultipartUploadStatus.UPLOADING);
        session.setUploadedPartCount(multipartUploadDao.countMultipartParts(session.getUploadId()));
        session.setUpdateDate(new Date());
        multipartUploadDao.updateMultipartSession(session);
        return part;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObject completeMultipartUpload(String uploadId, StoredObject object) {
        MultipartUploadSession session = requireActiveMultipartSession(uploadId);
        List<MultipartUploadPart> parts = multipartUploadDao.listMultipartParts(uploadId);
        validateMultipartParts(session, parts);

        StoredObject storage = toCompletedStorage(session, object);
        storage.setId(storedObjectDao.insert(storage));

        Date now = new Date();
        session.setUploadStatus(MultipartUploadStatus.COMPLETED);
        session.setUploadedPartCount(parts.size());
        session.setCompletedDate(now);
        session.setUpdateDate(now);
        multipartUploadDao.updateMultipartSession(session);
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
        return multipartUploadDao.updateMultipartSession(session);
    }

    private MultipartUploadSession requireActiveMultipartSession(String uploadId) {
        if (StringUtils.isBlank(uploadId)) {
            throw new BizException("Multipart upload id can not be empty");
        }
        MultipartUploadSession session = multipartUploadDao.getMultipartSessionByUploadId(uploadId);
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

    private StoredObject toCompletedStorage(MultipartUploadSession session, StoredObject object) {
        StoredObject storage = new StoredObject();
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
        storage.setObjectStatus(StoredObjectStatus.ACTIVE);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
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
