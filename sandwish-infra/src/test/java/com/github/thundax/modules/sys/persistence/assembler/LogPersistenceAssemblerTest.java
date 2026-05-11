package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import org.junit.Test;

public class LogPersistenceAssemblerTest {

    @Test
    public void shouldReadEnumType() {
        LogDO dataObject = new LogDO();
        dataObject.setType("ACCESS");

        Log entity = LogPersistenceAssembler.toEntity(dataObject);

        assertSame(LogType.ACCESS, entity.getType());
    }

    @Test
    public void shouldRejectLegacyNumericType() {
        LogDO dataObject = new LogDO();
        dataObject.setType("1");

        try {
            LogPersistenceAssembler.toEntity(dataObject);
            fail("Legacy log type value must be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Unknown log type: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteEnumTypeValue() {
        Log entity = new Log();
        entity.setType(LogType.EXCEPTION);

        LogDO dataObject = LogPersistenceAssembler.toDataObject(entity);

        assertEquals("EXCEPTION", dataObject.getType());
    }
}
