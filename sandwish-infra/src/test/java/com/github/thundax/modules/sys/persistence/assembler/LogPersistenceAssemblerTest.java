package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.LogType;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import org.junit.Test;

public class LogPersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyNumericType() {
        LogDO dataObject = new LogDO();
        dataObject.setType("1");

        Log entity = LogPersistenceAssembler.toEntity(dataObject);

        assertSame(LogType.ACCESS, entity.getType());
    }

    @Test
    public void shouldWriteEnumTypeValue() {
        Log entity = new Log();
        entity.setType(LogType.EXCEPTION);

        LogDO dataObject = LogPersistenceAssembler.toDataObject(entity);

        assertEquals("EXCEPTION", dataObject.getType());
    }
}
