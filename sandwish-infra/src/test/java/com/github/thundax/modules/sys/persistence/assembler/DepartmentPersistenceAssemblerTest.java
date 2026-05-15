package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.persistence.dataobject.DepartmentDO;
import org.junit.Test;

public class DepartmentPersistenceAssemblerTest {

    @Test
    public void shouldMapNameFieldsAtPersistenceBoundary() {
        Department entity = new Department();
        entity.setName("研发中心");
        entity.setShortName("R&D");
        DepartmentDO dataObject = new DepartmentDO();
        dataObject.setName("产品中心");
        dataObject.setShortName("Product");

        assertEquals("研发中心", DepartmentPersistenceAssembler.toDataObject(entity).getName());
        assertEquals("R&D", DepartmentPersistenceAssembler.toDataObject(entity).getShortName());
        assertEquals("产品中心", DepartmentPersistenceAssembler.toEntity(dataObject).getName());
        assertEquals(
                "Product", DepartmentPersistenceAssembler.toEntity(dataObject).getShortName());
    }
}
