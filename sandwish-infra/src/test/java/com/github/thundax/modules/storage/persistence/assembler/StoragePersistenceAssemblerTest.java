package com.github.thundax.modules.storage.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.*;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadPartId;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadSessionId;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadPartDO;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadSessionDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import java.util.Date;
import org.junit.Test;

public class StoragePersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyLowerCaseOwnerType() {
        StoredObjectDO dataObject = new StoredObjectDO();
        dataObject.setOwnerType("user");

        StoredObject entity = StoragePersistenceAssembler.toEntity(dataObject);

        assertSame(StorageOwnerType.USER, entity.getOwnerType());
    }

    @Test
    public void shouldWriteEnumOwnerTypeValue() {
        StoredObject entity = new StoredObject();
        entity.setOwnerType(StorageOwnerType.MEMBER);
        entity.setBucketName("/tmp/storage/");
        entity.setObjectKey("202605/s1.png");
        entity.setSize(10L);
        entity.setAccessEndpoint("/api/storage/object/s1/content");

        StoredObjectDO dataObject = StoragePersistenceAssembler.toDataObject(entity);

        assertEquals("MEMBER", dataObject.getOwnerType());
        assertEquals("/tmp/storage/", dataObject.getBucketName());
        assertEquals("202605/s1.png", dataObject.getObjectKey());
        assertEquals(Long.valueOf(10L), dataObject.getSize());
        assertEquals("/api/storage/object/s1/content", dataObject.getAccessEndpoint());
    }

    @Test
    public void shouldReadStatusesAsDomainValues() {
        StoredObjectDO dataObject = new StoredObjectDO();
        dataObject.setObjectStatus("ACTIVE");
        dataObject.setReferenceStatus("REFERENCED");

        StoredObject entity = StoragePersistenceAssembler.toEntity(dataObject);

        assertSame(StoredObjectStatus.ACTIVE, entity.getStatus());
        assertSame(StoredObjectReferenceStatus.REFERENCED, entity.getReferenceStatus());
    }

    @Test
    public void shouldRejectLegacyStatusValues() {
        StoredObjectDO dataObject = new StoredObjectDO();
        dataObject.setObjectStatus("1");
        dataObject.setReferenceStatus("REFERENCED");

        try {
            StoragePersistenceAssembler.toEntity(dataObject);
            fail("Legacy object status value must be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Unknown storage status: 1", expected.getMessage());
        }

        dataObject.setObjectStatus("ACTIVE");
        dataObject.setReferenceStatus("1");
        try {
            StoragePersistenceAssembler.toEntity(dataObject);
            fail("Legacy reference status value must be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Unknown storage reference status: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteDomainValuesToStatuses() {
        StoredObject entity = new StoredObject();
        entity.setStatus(StoredObjectStatus.DELETED);
        entity.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);

        StoredObjectDO dataObject = StoragePersistenceAssembler.toDataObject(entity);

        assertEquals("DELETED", dataObject.getObjectStatus());
        assertEquals("UNREFERENCED", dataObject.getReferenceStatus());
    }

    @Test
    public void shouldMapBusinessReferenceStatus() {
        StoredObjectReferenceDO dataObject = new StoredObjectReferenceDO();
        dataObject.setFileId(5001L);
        dataObject.setReferenceOwnerId("owner-1");
        dataObject.setReferenceOwnerType("USER");
        dataObject.setReferenceStatus("REFERENCED");

        StoredObjectReference entity = StoragePersistenceAssembler.toBusinessEntity(dataObject);

        assertEquals(Long.valueOf(5001L), entity.getId().value());
        assertEquals("owner-1", entity.getOwnerId());
        assertEquals("USER", entity.getOwnerType().value());
        assertEquals(
                Long.valueOf(5001L),
                StoragePersistenceAssembler.toBusinessDataObject(entity).getFileId());
        assertEquals(
                "owner-1",
                StoragePersistenceAssembler.toBusinessDataObject(entity).getReferenceOwnerId());
        assertSame(StoredObjectReferenceStatus.REFERENCED, entity.getReferenceStatus());
        assertEquals(
                "REFERENCED",
                StoragePersistenceAssembler.toBusinessDataObject(entity).getReferenceStatus());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        StoredObject entity = new StoredObject();
        entity.setPriority(-1);
        StoredObjectDO dataObject = new StoredObjectDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                StoragePersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, StoragePersistenceAssembler.toEntity(dataObject).getPriority());
    }

    @Test
    public void shouldMapMultipartSessionFields() {
        Date completedDate = new Date(3000L);
        Date abortedDate = new Date(4000L);
        MultipartUploadSession entity = new MultipartUploadSession();
        entity.setId(MultipartUploadSessionId.of(5002L));
        entity.setUploadId("upload-1");
        entity.setOwnerId("u1");
        entity.setOwnerType(StorageOwnerType.USER);
        entity.setBusinessType("product");
        entity.setOriginalFilename("demo.png");
        entity.setMimeType("image/png");
        entity.setBucketName("bucket-a");
        entity.setObjectKey("202605/demo.png");
        entity.setProviderUploadId("provider-1");
        entity.setTotalSize(1024L);
        entity.setPartSize(256L);
        entity.setUploadedPartCount(2);
        entity.setUploadStatus(MultipartUploadStatus.UPLOADING);
        entity.setCompletedDate(completedDate);
        entity.setAbortedDate(abortedDate);

        MultipartUploadSessionDO dataObject = StoragePersistenceAssembler.toMultipartSessionDataObject(entity);
        MultipartUploadSession restored = StoragePersistenceAssembler.toMultipartSessionEntity(dataObject);

        assertEquals(Long.valueOf(5002L), dataObject.getId());
        assertEquals("upload-1", dataObject.getUploadId());
        assertEquals("USER", dataObject.getOwnerType());
        assertEquals("UPLOADING", dataObject.getUploadStatus());
        assertEquals(Integer.valueOf(2), dataObject.getUploadedPartCount());
        assertEquals(Long.valueOf(5002L), restored.getId().value());
        assertSame(StorageOwnerType.USER, restored.getOwnerType());
        assertSame(MultipartUploadStatus.UPLOADING, restored.getUploadStatus());
        assertEquals("provider-1", restored.getProviderUploadId());
        assertEquals(completedDate, restored.getCompletedDate());
        assertEquals(abortedDate, restored.getAbortedDate());
    }

    @Test
    public void shouldReadLegacyLowerCaseMultipartSessionEnums() {
        MultipartUploadSessionDO dataObject = new MultipartUploadSessionDO();
        dataObject.setOwnerType("member");
        dataObject.setUploadStatus("initiated");

        MultipartUploadSession entity = StoragePersistenceAssembler.toMultipartSessionEntity(dataObject);

        assertSame(StorageOwnerType.MEMBER, entity.getOwnerType());
        assertSame(MultipartUploadStatus.INITIATED, entity.getUploadStatus());
        assertEquals(Integer.valueOf(0), entity.getUploadedPartCount());
    }

    @Test
    public void shouldMapMultipartPartFields() {
        MultipartUploadPart entity = new MultipartUploadPart();
        entity.setId(MultipartUploadPartId.of(5003L));
        entity.setUploadId("upload-1");
        entity.setPartNumber(1);
        entity.setEtag("etag-1");
        entity.setSize(128L);

        MultipartUploadPartDO dataObject = StoragePersistenceAssembler.toMultipartPartDataObject(entity);
        MultipartUploadPart restored = StoragePersistenceAssembler.toMultipartPartEntity(dataObject);

        assertEquals(Long.valueOf(5003L), dataObject.getId());
        assertEquals("upload-1", dataObject.getUploadId());
        assertEquals(Integer.valueOf(1), dataObject.getPartNumber());
        assertEquals("etag-1", dataObject.getEtag());
        assertEquals(Long.valueOf(128L), dataObject.getSize());
        assertEquals(Long.valueOf(5003L), restored.getId().value());
        assertEquals("upload-1", restored.getUploadId());
        assertEquals(Integer.valueOf(1), restored.getPartNumber());
        assertEquals("etag-1", restored.getEtag());
        assertEquals(Long.valueOf(128L), restored.getSize());
    }
}
