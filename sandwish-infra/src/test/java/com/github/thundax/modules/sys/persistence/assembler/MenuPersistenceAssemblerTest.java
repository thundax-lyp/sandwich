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
}
