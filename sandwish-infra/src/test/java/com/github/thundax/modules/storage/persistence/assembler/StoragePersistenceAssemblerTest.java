package com.github.thundax.modules.storage.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.config.Global;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import org.junit.Test;

public class StoragePersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyLowerCaseOwnerType() {
        StorageDO dataObject = new StorageDO();
        dataObject.setOwnerType("user");

        Storage entity = StoragePersistenceAssembler.toEntity(dataObject);

        assertSame(StorageOwnerType.USER, entity.getOwnerType());
    }

    @Test
    public void shouldWriteEnumOwnerTypeValue() {
        Storage entity = new Storage();
        entity.setOwnerType(StorageOwnerType.MEMBER);

        StorageDO dataObject = StoragePersistenceAssembler.toDataObject(entity);

        assertEquals("MEMBER", dataObject.getOwnerType());
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
        dataObject.setPublicFlag(Global.YES);

        StorageBusiness entity = StoragePersistenceAssembler.toBusinessEntity(dataObject);

        assertSame(StorageVisibility.PUBLIC, entity.getVisibility());
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
}
