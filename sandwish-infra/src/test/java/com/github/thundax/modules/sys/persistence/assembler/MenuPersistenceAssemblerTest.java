package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.config.Global;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import org.junit.Test;

public class MenuPersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyDisplayFlagAsDomainValue() {
        MenuDO dataObject = new MenuDO();
        dataObject.setDisplayFlag(Global.SHOW);

        Menu entity = MenuPersistenceAssembler.toEntity(dataObject);

        assertSame(MenuVisibility.VISIBLE, entity.getVisibility());
    }

    @Test
    public void shouldWriteDomainValueToLegacyDisplayFlag() {
        Menu entity = new Menu();
        entity.setVisibility(MenuVisibility.HIDDEN);

        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);

        assertEquals("HIDDEN", dataObject.getDisplayFlag());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Menu entity = new Menu();
        entity.setPriority(-1);
        MenuDO dataObject = new MenuDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                MenuPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, MenuPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
