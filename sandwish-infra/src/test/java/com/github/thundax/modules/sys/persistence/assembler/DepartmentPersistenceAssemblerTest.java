package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.persistence.dataobject.DepartmentDO;
import org.junit.Test;

public class DepartmentPersistenceAssemblerTest {

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Department entity = new Department();
        entity.setPriority(-1);
        DepartmentDO dataObject = new DepartmentDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                DepartmentPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, DepartmentPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
