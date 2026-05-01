package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import org.junit.Test;

public class DictPersistenceAssemblerTest {

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Dict entity = new Dict();
        entity.setPriority(-1);
        DictDO dataObject = new DictDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                DictPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, DictPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
