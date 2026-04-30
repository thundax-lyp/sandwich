package com.github.thundax.modules.storage.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageOwnerType;
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
}
