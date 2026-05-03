package com.github.thundax.modules.storage.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadPartDO;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadSessionDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import java.util.Date;
import org.junit.Test;

public class StoragePersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyLowerCaseOwnerType() {
        StorageDO dataObject = new StorageDO();
        dataObject.setOwnerType("user");
        dataObject.setStorageType("local_file");

        Storage entity = StoragePersistenceAssembler.toEntity(dataObject);

        assertSame(StorageOwnerType.USER, entity.getOwnerType());
        assertSame(StorageBackendType.LOCAL_FILE, entity.getStorageType());
    }

    @Test
    public void shouldWriteEnumOwnerTypeValue() {
        Storage entity = new Storage();
        entity.setOwnerType(StorageOwnerType.MEMBER);
        entity.setStorageType(StorageBackendType.LOCAL_FILE);
        entity.setBucketName("/tmp/storage/");
        entity.setObjectKey("202605/s1.png");
        entity.setSize(10L);
        entity.setAccessEndpoint("/servlet/storage/s1.png");

        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);

        assertEquals("MEMBER", dataObject.getOwnerType());
        assertEquals("LOCAL_FILE", dataObject.getStorageType());
        assertEquals("/tmp/storage/", dataObject.getBucketName());
        assertEquals("202605/s1.png", dataObject.getObjectKey());
        assertEquals(Long.valueOf(10L), dataObject.getSize());
        assertEquals("/servlet/storage/s1.png", dataObject.getAccessEndpoint());
    }

    @Test
    public void shouldReadLegacyFlagsAsDomainValues() {
        StorageDO dataObject = new StorageDO();
        dataObject.setEnableFlag(Global.ENABLE);
        dataObject.setPublicFlag(Global.YES);

        Storage entity = StoragePersistenceAssembler.toEntity(dataObject);

        assertSame(StorageStatus.ENABLED, entity.getStatus());
        assertSame(StorageVisibility.PUBLIC, entity.getVisibility());
    }

    @Test
    public void shouldWriteDomainValuesToLegacyFlags() {
        Storage entity = new Storage();
        entity.setStatus(StorageStatus.DISABLED);
        entity.setVisibility(StorageVisibility.PRIVATE);

        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);

        assertEquals("DISABLED", dataObject.getEnableFlag());
        assertEquals("PRIVATE", dataObject.getPublicFlag());
    }

    @Test
    public void shouldMapBusinessVisibility() {
        StorageBusinessDO dataObject = new StorageBusinessDO();
        dataObject.setFileId("s1");
        dataObject.setPublicFlag(Global.YES);

        StorageBusiness entity = StoragePersistenceAssembler.toBusinessEntity(dataObject);

        assertEquals("s1", entity.getId().value());
        assertSame(StorageVisibility.PUBLIC, entity.getVisibility());
        assertEquals(
                "s1", StoragePersistenceAssembler.toBusinessDataObject(entity).getFileId());
        assertEquals(
                "PUBLIC",
                StoragePersistenceAssembler.toBusinessDataObject(entity).getPublicFlag());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Storage entity = new Storage();
        entity.setPriority(-1);
        StorageDO dataObject = new StorageDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                StoragePersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, StoragePersistenceAssembler.toEntity(dataObject).getPriority());
    }

    @Test
    public void shouldMapMultipartSessionFields() {
        Date createDate = new Date(1000L);
        Date updateDate = new Date(2000L);
        Date completedDate = new Date(3000L);
        Date abortedDate = new Date(4000L);
        MultipartUploadSession entity = new MultipartUploadSession();
        entity.setId(EntityIdCodec.toDomain("ms1"));
        entity.setUploadId("upload-1");
        entity.setOwnerId("u1");
        entity.setOwnerType(StorageOwnerType.USER);
        entity.setBusinessType("product");
        entity.setOriginalFilename("demo.png");
        entity.setMimeType("image/png");
        entity.setStorageType(StorageBackendType.OSS);
        entity.setBucketName("bucket-a");
        entity.setObjectKey("202605/demo.png");
        entity.setProviderUploadId("provider-1");
        entity.setTotalSize(1024L);
        entity.setPartSize(256L);
        entity.setUploadedPartCount(2);
        entity.setUploadStatus(MultipartUploadStatus.UPLOADING);
        entity.setCreateDate(createDate);
        entity.setUpdateDate(updateDate);
        entity.setCompletedDate(completedDate);
        entity.setAbortedDate(abortedDate);

        MultipartUploadSessionDO dataObject = StoragePersistenceAssembler.toMultipartSessionDataObject(entity);
        MultipartUploadSession restored = StoragePersistenceAssembler.toMultipartSessionEntity(dataObject);

        assertEquals("ms1", dataObject.getId());
        assertEquals("upload-1", dataObject.getUploadId());
        assertEquals("USER", dataObject.getOwnerType());
        assertEquals("OSS", dataObject.getStorageType());
        assertEquals("UPLOADING", dataObject.getUploadStatus());
        assertEquals(Integer.valueOf(2), dataObject.getUploadedPartCount());
        assertEquals("ms1", restored.getId().value());
        assertSame(StorageOwnerType.USER, restored.getOwnerType());
        assertSame(StorageBackendType.OSS, restored.getStorageType());
        assertSame(MultipartUploadStatus.UPLOADING, restored.getUploadStatus());
        assertEquals("provider-1", restored.getProviderUploadId());
        assertEquals(completedDate, restored.getCompletedDate());
        assertEquals(abortedDate, restored.getAbortedDate());
    }

    @Test
    public void shouldReadLegacyLowerCaseMultipartSessionEnums() {
        MultipartUploadSessionDO dataObject = new MultipartUploadSessionDO();
        dataObject.setOwnerType("member");
        dataObject.setStorageType("local_file");
        dataObject.setUploadStatus("initiated");

        MultipartUploadSession entity = StoragePersistenceAssembler.toMultipartSessionEntity(dataObject);

        assertSame(StorageOwnerType.MEMBER, entity.getOwnerType());
        assertSame(StorageBackendType.LOCAL_FILE, entity.getStorageType());
        assertSame(MultipartUploadStatus.INITIATED, entity.getUploadStatus());
        assertEquals(Integer.valueOf(0), entity.getUploadedPartCount());
    }

    @Test
    public void shouldMapMultipartPartFields() {
        Date createDate = new Date(1000L);
        MultipartUploadPart entity = new MultipartUploadPart();
        entity.setId(EntityIdCodec.toDomain("mp1"));
        entity.setUploadId("upload-1");
        entity.setPartNumber(1);
        entity.setEtag("etag-1");
        entity.setSize(128L);
        entity.setCreateDate(createDate);

        MultipartUploadPartDO dataObject = StoragePersistenceAssembler.toMultipartPartDataObject(entity);
        MultipartUploadPart restored = StoragePersistenceAssembler.toMultipartPartEntity(dataObject);

        assertEquals("mp1", dataObject.getId());
        assertEquals("upload-1", dataObject.getUploadId());
        assertEquals(Integer.valueOf(1), dataObject.getPartNumber());
        assertEquals("etag-1", dataObject.getEtag());
        assertEquals(Long.valueOf(128L), dataObject.getSize());
        assertEquals(createDate, dataObject.getCreateDate());
        assertEquals("mp1", restored.getId().value());
        assertEquals("upload-1", restored.getUploadId());
        assertEquals(Integer.valueOf(1), restored.getPartNumber());
        assertEquals("etag-1", restored.getEtag());
        assertEquals(Long.valueOf(128L), restored.getSize());
        assertEquals(createDate, restored.getCreateDate());
    }
}
