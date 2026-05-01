package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;
import org.junit.Test;

public class OfficePersistenceAssemblerTest {

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Office entity = new Office();
        entity.setPriority(-1);
        OfficeDO dataObject = new OfficeDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                OfficePersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, OfficePersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
